package com.qoid.bennu.model

import com.qoid.bennu.ErrorCode
import com.qoid.bennu.JsonAssist._
import com.qoid.bennu.ToJsonCapable
import m3.predef._
import m3.servlet.longpoll.{ChannelManager, ChannelId}

case class AsyncResponse(
  responseType: AsyncResponseType,
  handle: InternalId,
  success: Boolean,
  data: JValue,
  errorCode: Option[ErrorCode] = None,
  errorMsg: Option[String] = None,
  stacktrace: Option[String] = None
) extends ToJsonCapable {

  def send(channelId: ChannelId): Unit = {
    inject[ChannelManager].channel(channelId).put(this.toJson)
  }
}
