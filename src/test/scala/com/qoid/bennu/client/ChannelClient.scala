package com.qoid.bennu.client

import com.qoid.bennu.JsonAssist._
import com.qoid.bennu.model.Alias
import com.qoid.bennu.model.id.InternalId
import m3.servlet.beans.MultiRequestHandler.MethodInvocationResult

import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scala.concurrent.Promise

trait ChannelClient extends ServiceAssist with QueryAssist with IntroductionAssist {
  implicit val ec: ExecutionContext
  val alias: Alias

  // Submits a request over the channel. Any responses are passed to fn.
  def submit(
    path: String,
    parms: Map[String, JValue],
    context: JValue = JString(InternalId.random.value)
  )(
    fn: MethodInvocationResult => Unit
  ): Unit

  // Submits a request that only expects one response over the channel.
  // The response result is returned as the completion of the promise.
  def submitSingleResponse(path: String, parms: Map[String, JValue]): Future[JValue] = {
    val promise = Promise[JValue]()

    submit(path, parms) { result =>
      cancelSubmit(result.context)

      (result.success, result.error) match {
        case (true, _) => promise.success(result.result)
        case (false, Some(error)) => promise.failure(new Exception(error.message))
        case (false, None) => promise.failure(new Exception("Failed with no error"))
      }
    }

    promise.future
  }

  // Stops waiting for a response to a request.
  def cancelSubmit(context: JValue): Unit

  // Posts a request and returns the service response
  def post(path: String, parms: Map[String, JValue]): Future[JValue]

  // Closes the channel
  def close(): Unit
}
