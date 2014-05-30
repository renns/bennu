package com.qoid.bennu.client

import com.qoid.bennu.JsonAssist._

case class ChannelRequestRequest(
  path: String,
  context: JValue,
  parms: JValue
)
