package com.qoid.bennu.distributed.messages

import com.qoid.bennu.FromJsonCapable
import com.qoid.bennu.JsonAssist._
import com.qoid.bennu.ToJsonCapable

object DistributedMessage extends FromJsonCapable[DistributedMessage]

case class DistributedMessage(
  kind: DistributedMessageKind,
  version: Int,
  data: JValue
) extends ToJsonCapable
