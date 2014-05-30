package com.qoid.bennu.client

import com.qoid.bennu.ToJsonCapable
import m3.servlet.longpoll.ChannelId

case class ChannelRequest(
  channel: ChannelId,
  requests: List[ChannelRequestRequest]
) extends ToJsonCapable
