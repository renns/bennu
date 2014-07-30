package com.qoid.bennu.client

import com.qoid.bennu.JsonAssist._
import com.qoid.bennu.model.id.InternalId
import m3.servlet.beans.MultiRequestHandler.MethodInvocationResult

import scala.concurrent.ExecutionContext
import scala.concurrent.Future

trait ChannelClient extends ServiceAssist with QueryAssist {
  implicit val ec: ExecutionContext
  val connectionIid: InternalId

  // Submits a request over the channel. Any responses are passed to fn.
  def submit(
    path: String,
    parms: Map[String, JValue],
    context: JValue = JString(InternalId.random.value)
  )(
    fn: MethodInvocationResult => Unit
  ): Unit

  // Stops waiting for a response to a request.
  def cancelSubmit(context: JValue): Unit

  // Posts a request and returns the service response
  def post(path: String, parms: Map[String, JValue]): Future[JValue]

  // Closes the channel
  def close(): Unit
}
