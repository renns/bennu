package com.qoid.bennu.client

import com.qoid.bennu.JsonAssist._
import com.qoid.bennu.ServicePath
import com.qoid.bennu.model._
import com.qoid.bennu.model.id.InternalId
import java.util.UUID
import m3.LockFreeMap
import m3.predef._
import m3.servlet.longpoll.ChannelId
import scala.async.Async._
import scala.concurrent._

case class HttpChannelClient(
  agentName: String,
  channelId: ChannelId,
  rootAliasIid: InternalId
)(
  implicit
  config: HttpClientConfig,
  val ec: ExecutionContext
) extends ChannelClient with HttpAssist with Logging {

  private val waiters = new LockFreeMap[JValue, Promise[ChannelResponse]]
  private var closed = false

  poll()

  override def post(
    path: String,
    parms: Map[String, JValue],
    context0: JValue = JString(InternalId.random.value)
  ): Future[ChannelResponse] = {
    val promise = Promise[ChannelResponse]()

    try {
      val context = context0 match {
        case JNothing => JString(UUID.randomUUID().toString.replaceAll("-", ""))
        case jv => jv
      }
      val request = createRequest(path, context, parms)

      waiters += context -> promise

      promise.future.onFailure {
        case _ => waiters -= context
      }

      httpPost(s"${config.server}${ServicePath.submitChannel}", request.toJson, Some(s"channel=${channelId.value}"))
    } catch {
      case e: Exception => promise.failure(e)
    }

    promise.future
  }

  override def close(): Unit = {
    logout()
    closed = true
  }

  private def createRequest(path: String, context: JValue, parms: Map[String, JValue]): ChannelRequest = {
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

  private def poll(): Unit = {
    val path =
      config.server +
      ServicePath.pollChannel + "/" +
      channelId.value + "/" +
      config.pollTimeout.inMilliseconds().toString

    httpGet(path).foreach(process)
  }

  private def process(responseBody: String): Unit = {
    parseJson(responseBody) match {
      case JArray(messages) =>
        for (message <- messages) {
          logger.debug(s"received \n  <--- \n${message.toJsonStr.indent("        ")}")
          message \ "handle" match {
            case JNothing =>
              // This is a channel response
              val channelResponse = serializer.fromJson[ChannelResponse](message)
              waiters.remove(channelResponse.context).foreach(_.success(channelResponse))
            case _ =>
              // This is an async response
              val response = serializer.fromJson[QueryResponse](message)

              for (callback <- asyncCallbacks.get(response.context)) {
                async {
                  callback(response)
                }
              }
          }
        }
      case _ => logger.warn(s"channel poll response invalid -- $responseBody")
    }

    if (!closed) {
      poll()
    }
  }
}
