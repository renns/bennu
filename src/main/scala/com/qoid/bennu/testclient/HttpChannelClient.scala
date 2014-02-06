package com.qoid.bennu.testclient

import com.qoid.bennu.{JdbcAssist, JsonAssist}
import com.qoid.bennu.model._
import java.util.UUID
import m3.LockFreeMap
import m3.json.LiftJsonAssist._
import m3.predef._
import m3.servlet.longpoll.ChannelId
import net.liftweb.json.JArray
import net.model3.lang.TimeDuration
import org.apache.http.client.methods._
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.HttpClientBuilder
import scala.collection.immutable.HashMap
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent._
import com.qoid.bennu.squery.StandingQueryEvent

class HttpChannelClient(host: String, channelId: ChannelId) extends ChannelClient {
  private val waiters = new LockFreeMap[String, Promise[ChannelResponse]]
  private val squeryCallbacks = new LockFreeMap[InternalId, (InternalId, HasInternalId) => Unit]
  private val pollTimeout = new TimeDuration("10 seconds")

  spawnLongPoller()

  override def post(path: String, parms: Map[String, JValue]): Future[ChannelResponse] = {
    val promise = Promise[ChannelResponse]()

    future {
      try {
      val client = HttpClientBuilder.create.build
      val httpPost = new HttpPost(host + ApiPath.submitChannel)
      val context = UUID.randomUUID().toString.replaceAll("-", "")
      val request = createRequest(path, context, parms)

      httpPost.setHeader("Cookie", s"channel=${channelId.value}")
      httpPost.setHeader("Content-Type", "application/json")
      httpPost.setEntity(new StringEntity(request.toJson.toJsonStr))

      waiters += context -> promise

      promise.future.onFailure {
        case _ => waiters -= context
      }

      val response = client.execute(httpPost)

      if (response.getStatusLine.getStatusCode != 200) {
        promise.failure(new Exception(s"Post to $path failed."))
      }
      } catch {
        case e: Exception => println(e)
      }
    }

    promise.future
  }

  override def registerStandingQuery(types: List[String])(callback: (InternalId, HasInternalId) => Unit): Future[InternalId] = {
    val parms = HashMap("types" -> JArray(types.map(JString(_))))
    post(ApiPath.registerStandingQuery, parms) map {
      _.result match {
        case JObject(JField("handle", JString(handle)) :: Nil) =>
          squeryCallbacks += InternalId(handle) -> callback
          InternalId(handle)
        case r => throw new Exception(s"Result from ${ApiPath.registerStandingQuery} is invalid: " + r.toJsonStr)
      }
    }
  }

  override def deRegisterStandingQuery(handle: InternalId): Future[Boolean] = {
    squeryCallbacks -= handle
    val parms = HashMap("handle" -> JString(handle.value))
    post(ApiPath.deRegisterStandingQuery, parms).map(_.success)
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
    spawn(s"long-poller-${channelId.value}"){
      while (true) {
        longPoll()
      }
    }
  }

  private def longPoll(): Unit = {
    val client = HttpClientBuilder.create.build
    val httpGet = new HttpGet(s"$host${ApiPath.pollChannel}/${channelId.value}/${pollTimeout.inMilliseconds().toString}")
    val response = client.execute(httpGet)
    val responseBody = response.getEntity.getContent.readString

    parseJson(responseBody) match {
      case JArray(messages) =>
        for (message <- messages) {
          message \ "success" match {
            case JNothing =>
              // This is a standing query event
              val event = JsonAssist.serializer.fromJson[StandingQueryEvent](message)
              val mapper = JdbcAssist.findMapperByTypeName(event.`type`).asInstanceOf[JdbcAssist.BennuMapperCompanion[HasInternalId]]
              val instance = mapper.fromJson(event.instance)
              squeryCallbacks.get(event.handle).foreach(_(event.handle, instance))
            case _ =>
              // This is a channel response
              val channelResponse = JsonAssist.serializer.fromJson[ChannelResponse](message)
              waiters.remove(channelResponse.context).foreach(_.success(channelResponse))
          }
        }
      case _ => () //TODO: log an error with responseBody
    }
  }
}
