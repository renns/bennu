package com.qoid.bennu.distributed.messages

import com.qoid.bennu.FromJsonCapable
import com.qoid.bennu.JsonAssist._
import com.qoid.bennu.ToJsonCapable
import com.qoid.bennu.model.id.DistributedMessageId
import com.qoid.bennu.model.id.InternalId

object DistributedMessage extends FromJsonCapable[DistributedMessage]

case class DistributedMessage(
  kind: DistributedMessageKind,
  version: Int,
  route: List[InternalId],
  data: JValue,
  replyToMessageId: Option[DistributedMessageId] = None,
  replyRoute: List[InternalId] = Nil,
  messageId: DistributedMessageId = DistributedMessageId.random
) extends ToJsonCapable
