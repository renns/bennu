package com.qoid.bennu.testclient.client

import com.qoid.bennu.JdbcAssist
import com.qoid.bennu.JsonAssist
import com.qoid.bennu.ServicePath
import com.qoid.bennu.model._
import com.qoid.bennu.squery.StandingQueryEvent
import com.qoid.bennu.testclient.client.HttpAssist.HttpClientConfig
import java.util.UUID
import m3.LockFreeMap
import m3.json.LiftJsonAssist._
import m3.predef._
import m3.servlet.longpoll.ChannelId
import scala.concurrent._
import scala.concurrent.duration.Duration

case class HttpChannelClient(
  agentId: AgentId,
  channelId: ChannelId
)(
  implicit config: HttpClientConfig
) extends ChannelClient with HttpAssist with Logging {

  private val waiters = new LockFreeMap[String, Promise[ChannelResponse]]

  spawnLongPoller()

  override def postAsync(path: String, parms: Map[String, JValue])(implicit ec: ExecutionContext): Future[ChannelResponse] = {
    val promise = Promise[ChannelResponse]()

    future {
      try {
        val context = UUID.randomUUID().toString.replaceAll("-", "")
        val request = createRequest(path, context, parms)

        waiters += context -> promise

        promise.future.onFailure {
          case _ => waiters -= context
        }

        httpPost(s"${config.server}${ServicePath.submitChannel}", request.toJson, Some(s"channel=${channelId.value}"))
      } catch {
        case e: Exception => promise.failure(e)
      }
    }

    promise.future
  }

  override def post(path: String, parms: Map[String, JValue]): ChannelResponse = {
    import scala.concurrent.ExecutionContext.Implicits.global
    Await.result(postAsync(path, parms), Duration(s"${config.requestTimeout.inSeconds()} seconds"))
  }

  private def createRequest(path: String, context: String, parms: Map[String, JValue]): ChannelRequest = {
    val parmsJValue = JObject(parms.map{
      case (k, v) => JField(k, v)
    }.toList)

    ChannelRequest(
      channelId,
      List(ChannelRequestRequest(
        path,
        context,
        parmsJValue
      ))
    )
  }

  private def spawnLongPoller(): Unit = {
    spawn(s"long-poller-${channelId.value}") {
      while (true) {
        longPoll()
      }
    }
  }

  private def longPoll(): Unit = {
    val responseBody = httpGet(s"${config.server}${ServicePath.pollChannel}/${channelId.value}/${config.pollTimeout.inMilliseconds().toString}")

    parseJson(responseBody) match {
      case JArray(messages) =>
        for (message <- messages) {
          message \ "context" match {
            case JNothing =>
              // This is an async response
              val response = JsonAssist.serializer.fromJson[AsyncResponse](message)

              response.responseType match {
                case AsyncResponseType.SQuery =>
                  // This is an squery response
                  if (response.success) {
                    val event = JsonAssist.serializer.fromJson[StandingQueryEvent](response.data)
                    val mapper = JdbcAssist.findMapperByTypeName(event.tpe).asInstanceOf[JdbcAssist.BennuMapperCompanion[HasInternalId]]
                    val instance = mapper.fromJson(event.instance)

                    for (callback <- squeryCallbacks.get(response.handle)) {
                      spawn(s"squery-${response.handle}") {
                        callback(event.action, response.handle, instance)
                      }
                    }
                  } else {
                    logger.warn(s"failed AsyncResponse -- $response")
                  }
                case _ =>
                  // This is any async response other than squery
                  for (callback <- asyncCallbacks.get(response.handle)) {
                    spawn(s"async-${response.handle}") {
                      callback(response)
                    }
                  }
              }
            case _ =>
              // This is a channel response
              val channelResponse = JsonAssist.serializer.fromJson[ChannelResponse](message)
              waiters.remove(channelResponse.context).foreach(_.success(channelResponse))
          }
        }
      case _ => logger.warn(s"channel poll response invalid -- $responseBody")
    }
  }
}
