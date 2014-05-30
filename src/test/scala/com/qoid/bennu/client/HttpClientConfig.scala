package com.qoid.bennu.client

import net.model3.lang.TimeDuration

case class HttpClientConfig(
  server: String = "http://localhost:8080",
  pollTimeout: TimeDuration = new TimeDuration("5 s")
)
