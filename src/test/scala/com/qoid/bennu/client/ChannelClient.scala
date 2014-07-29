package com.qoid.bennu.client

import com.qoid.bennu.JsonAssist._
import com.qoid.bennu.distributed.messages.StandingQueryResponse
import com.qoid.bennu.model.id.InternalId
import m3.servlet.beans.MultiRequestHandler.MethodInvocationResult

import scala.concurrent.ExecutionContext
import scala.concurrent.Future

trait ChannelClient extends ServiceAssist with QueryAssist {
  implicit val ec: ExecutionContext
  val connectionIid: InternalId

  // Submits a request over the channel and returns the channel response corresponding to the context
  def submit(
    path: String,
    parms: Map[String, JValue],
    context: JValue = JString(InternalId.random.value)
  ): Future[MethodInvocationResult]

  // Submits a request over the channel and returns the channel response corresponding to the context
  // Any standing responses are sent to fn
  def submitStanding(
    path: String,
    parms: Map[String, JValue],
    context: JValue = JString(InternalId.random.value)
  )(
    fn: (StandingQueryResponse, JValue) => Unit
  ): Future[MethodInvocationResult]

  // Posts a request and returns the service response
  def post(path: String, parms: Map[String, JValue]): Future[JValue]

  // Closes the channel
  def close(): Unit
}
