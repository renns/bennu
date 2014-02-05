package com.qoid.bennu.testclient

import com.qoid.bennu.JsonAssist
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
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent._

class HttpChannelClient(host: String, channelId: ChannelId) extends ChannelClient {
  private val waiters = new LockFreeMap[String, Promise[ChannelResponse]]
  private val pollTimeout = new TimeDuration("10 seconds")

  spawnLongPoller()

  override def post(path: String, parms: Map[String, JValue]): Future[ChannelResponse] = {
    val promise = Promise[ChannelResponse]()

    future {
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
    }

    promise.future
  }

  private def createRequest(path: String, context: String, parms: Map[String, JValue]): ChannelRequest = {
    val parmsJValue = parms.map{
      case (k, v) => JField(k, v)
    }.foldLeft[JValue](JNothing)(_ ++ _)

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
          val channelResponse = JsonAssist.serializer.fromJson[ChannelResponse](message)
          waiters.remove(channelResponse.context).foreach(_.success(channelResponse))
        }
      case _ => () //TODO: log an error with responseBody
    }
  }
}
