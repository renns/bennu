package com.qoid.bennu.client

import com.qoid.bennu.JsonAssist._
import com.qoid.bennu.model.id.InternalId

import scala.concurrent.ExecutionContext
import scala.concurrent.Future

trait ChannelClient extends ServiceAssist {
  implicit val ec: ExecutionContext

  def submit(
    path: String,
    parms: Map[String, JValue],
    context: JValue = JString(InternalId.random.value)
  ): Future[ChannelResponse]

  def post(path: String, parms: Map[String, JValue]): Future[JValue]

  def close(): Unit
}
