package com.qoid.bennu.client

import com.qoid.bennu.JsonAssist._

case class ChannelResponse(
  handle: Option[String],
  data: JValue,
  responseType: Option[String],
  success: Boolean,
  context: JValue,
  result: JValue,
  error: Option[ChannelResponseError]
)
