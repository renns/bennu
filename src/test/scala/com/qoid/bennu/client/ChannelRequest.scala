package com.qoid.bennu.client

import com.qoid.bennu.ToJsonCapable

case class ChannelRequest(
  requests: List[ChannelRequestRequest]
) extends ToJsonCapable
