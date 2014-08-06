package com.qoid.bennu.client

import com.qoid.bennu.JsonAssist._
import com.qoid.bennu.JsonAssist.jsondsl._
import com.qoid.bennu.model.id.InternalId
import com.qoid.bennu.webservices.ServicePath
import m3.LockFreeMap
import m3.predef._
import m3.servlet.beans.MultiRequestHandler.MethodInvocation
import m3.servlet.beans.MultiRequestHandler.MethodInvocationResult
import m3.servlet.longpoll.ChannelId

import scala.concurrent.ExecutionContext
import scala.concurrent.Future

case class HttpChannelClient(
  channelId: ChannelId,
  connectionIid: InternalId
)(
  implicit
  config: HttpClientConfig,
  val ec: ExecutionContext
) extends ChannelClient with Logging {

  private val callbacks = new LockFreeMap[JValue, MethodInvocationResult => Unit]
  private var closed = false

  poll()

  override def submit(
    path: String,
    parms: Map[String, JValue],
    context: JValue = JString(InternalId.random.value)
  )(
    fn: MethodInvocationResult => Unit
  ): Unit = {
    val parmsJValue = JObject(parms.map{ case (k, v) => JField(k, v) }.toList)
    val request = serializer.toJson(MethodInvocation(path, context, parmsJValue))
    val body: JValue = "requests" -> List(request)

    callbacks.put(context, fn)

    HttpAssist.httpPost(s"${config.server}${ServicePath.submitChannelRequests}", body, channelId)
  }

  override def cancelSubmit(context: JValue): Unit = {
    callbacks.remove(context)
  }

  override def post(path: String, parms: Map[String, JValue]): Future[JValue] = {
    val parmsJValue = JObject(parms.map{ case (k, v) => JField(k, v) }.toList)
    HttpAssist.httpPost(s"${config.server}${path}", parmsJValue, channelId).map(parseJson)
  }

  override def close(): Unit = {
    closed = true
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
          val result = serializer.fromJson[MethodInvocationResult](message)

          if (!result.success || result.result != JNothing) {
            callbacks.get(result.context).foreach(_(result))
          }
        }
      case _ => logger.warn(s"channel poll response invalid -- $responseBody")
    }

    if (!closed) {
      poll()
    }
  }
}
