package com.qoid.bennu.client

import com.qoid.bennu.JsonAssist._
import com.qoid.bennu.JsonAssist.jsondsl._
import com.qoid.bennu.ServicePath
import com.qoid.bennu.distributed.DistributedResult
import com.qoid.bennu.distributed.messages.DistributedMessageKind
import com.qoid.bennu.distributed.messages.StandingQueryResponse
import com.qoid.bennu.model.id.InternalId
import m3.LockFreeMap
import m3.predef._
import m3.servlet.beans.MultiRequestHandler.MethodInvocation
import m3.servlet.beans.MultiRequestHandler.MethodInvocationResult
import m3.servlet.longpoll.ChannelId

import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scala.concurrent.Promise

case class HttpChannelClient(
  channelId: ChannelId,
  connectionIid: InternalId
)(
  implicit
  config: HttpClientConfig,
  val ec: ExecutionContext
) extends ChannelClient with Logging {

  private val standingQueryCallbacks = new LockFreeMap[JValue, (StandingQueryResponse, JValue) => Unit]
  private val waiters = new LockFreeMap[JValue, Promise[MethodInvocationResult]]
  private var closed = false

  poll()

  override def submit(
    path: String,
    parms: Map[String, JValue],
    context: JValue = JString(InternalId.random.value)
  ): Future[MethodInvocationResult] = {
    val promise = Promise[MethodInvocationResult]()

    try {
      val parmsJValue = JObject(parms.map{ case (k, v) => JField(k, v) }.toList)
      val request = serializer.toJson(MethodInvocation(path, context, parmsJValue))
      val body: JValue = "requests" -> List(request)

      waiters += context -> promise

      promise.future.onFailure {
        case _ => waiters -= context
      }

      HttpAssist.httpPost(s"${config.server}${ServicePath.submitChannelRequests}", body, channelId)
    } catch {
      case e: Exception => promise.failure(e)
    }

    promise.future
  }

  override def submitStanding(
    path: String,
    parms: Map[String, JValue],
    context: JValue = JString(InternalId.random.value)
  )(
    fn: (StandingQueryResponse, JValue) => Unit
  ): Future[MethodInvocationResult] = {

    standingQueryCallbacks.put(context, fn)
    submit(path, parms, context)
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
          val result1 = serializer.fromJson[MethodInvocationResult](message)

          if (!result1.success) {
            waiters.remove(result1.context).foreach(_.success(result1))
          } else if (result1.result != JNothing) {
            val result2 = DistributedResult.fromJson(result1.result)

            if (result2.kind == DistributedMessageKind.StandingQueryResponse) {
              val result3 = StandingQueryResponse.fromJson(result2.result)
              standingQueryCallbacks.get(result1.context).foreach(_(result3, result1.context))
            } else {
              waiters.remove(result1.context).foreach(_.success(result1))
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
