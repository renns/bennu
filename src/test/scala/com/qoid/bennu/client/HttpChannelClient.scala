package com.qoid.bennu.client

import com.qoid.bennu.JsonAssist._
import com.qoid.bennu.ServicePath
import com.qoid.bennu.model.id.InternalId
import com.qoid.bennu.query.QueryManager.QueryResponse
import m3.LockFreeMap
import m3.predef._
import m3.servlet.longpoll.ChannelId

import scala.async.Async.async
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scala.concurrent.Promise

case class HttpChannelClient(
  channelId: ChannelId
)(
  implicit
  config: HttpClientConfig,
  val ec: ExecutionContext
) extends ChannelClient with Logging {

  private val asyncCallbacks = new LockFreeMap[JValue, QueryResponse => Unit]
  private val waiters = new LockFreeMap[JValue, Promise[ChannelResponse]]
  private var closed = false

  poll()

  override def submit(
    path: String,
    parms: Map[String, JValue],
    context: JValue = JString(InternalId.random.value)
  ): Future[ChannelResponse] = {
    val promise = Promise[ChannelResponse]()

    try {
      val request = createRequest(path, context, parms)

      waiters += context -> promise

      promise.future.onFailure {
        case _ => waiters -= context
      }

      HttpAssist.httpPost(s"${config.server}${ServicePath.submitChannelRequests}", request.toJson, channelId)
    } catch {
      case e: Exception => promise.failure(e)
    }

    promise.future
  }

  override def post(path: String, parms: Map[String, JValue]): Future[JValue] = {
    val parmsJValue = JObject(parms.map{ case (k, v) => JField(k, v) }.toList)
    HttpAssist.httpPost(s"${config.server}${path}", parmsJValue, channelId).map(parseJson)
  }

  override def close(): Unit = {
    closed = true
  }

  private def createRequest(path: String, context: JValue, parms: Map[String, JValue]): ChannelRequest = {
    val parmsJValue = JObject(parms.map{ case (k, v) => JField(k, v) }.toList)

    ChannelRequest(
      List(ChannelRequestRequest(
        path,
        context,
        parmsJValue
      ))
    )
  }

  private def poll(): Unit = {
    HttpAssist.httpPost(
      s"${config.server}${ServicePath.pollChannel}/${config.pollTimeout.inMilliseconds().toString}",
      JNothing,
      channelId
    ).foreach(process)
  }

  private def process(responseBody: String): Unit = {
    parseJson(responseBody) match {
      case JArray(messages) =>
        for (message <- messages) {
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
