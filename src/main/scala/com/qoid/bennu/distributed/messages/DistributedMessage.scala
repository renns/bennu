package com.qoid.bennu.distributed.messages

import com.qoid.bennu.JsonAssist._
import com.qoid.bennu.ToJsonCapable

case class DistributedMessage(
  kind: DistributedMessageKind,
  version: Int,
  data: JValue
) extends ToJsonCapable
