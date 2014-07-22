package com.qoid.bennu.query

import com.qoid.bennu.model.id.AgentId
import com.qoid.bennu.model.id.DistributedMessageId
import com.qoid.bennu.model.id.InternalId

case class StandingQuery(
  agentId: AgentId,
  tpe: String,
  query: String,
  messageId: DistributedMessageId,
  replyRoute: List[InternalId]
)
