package com.qoid.bennu.model

import com.qoid.bennu.ErrorCode
import com.qoid.bennu.JsonAssist._
import com.qoid.bennu.ToJsonCapable
import m3.predef._
import m3.servlet.longpoll.ChannelId
import m3.servlet.longpoll.ChannelManager

case class AsyncResponse(
  responseType: AsyncResponseType,
  handle: Handle,
  success: Boolean,
  data: JValue,
  context: JValue,
  error: Option[AsyncResponseError] = None
) extends ToJsonCapable {

  def send(channelId: ChannelId): Unit = {
    inject[ChannelManager].channel(channelId).put(this.toJson)
  }
}

case class AsyncResponseError(
  code: ErrorCode,
  message: String,
  stacktrace: Option[String] = None
)
