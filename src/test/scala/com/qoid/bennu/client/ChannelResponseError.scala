package com.qoid.bennu.client

case class ChannelResponseError(
  message: Option[String],
  stacktrace: String
)
