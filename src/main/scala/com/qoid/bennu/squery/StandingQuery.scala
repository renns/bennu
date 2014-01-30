package com.qoid.bennu.squery

import com.qoid.bennu.model._
import m3.servlet.longpoll.ChannelId

case class StandingQuery(
  agentId: AgentId,
  channelId: ChannelId,
  handle: InternalId,
  tpe: String
)
