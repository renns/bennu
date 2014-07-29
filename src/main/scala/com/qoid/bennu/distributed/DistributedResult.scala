package com.qoid.bennu.distributed

import com.qoid.bennu.FromJsonCapable
import com.qoid.bennu.JsonAssist._
import com.qoid.bennu.ToJsonCapable
import com.qoid.bennu.distributed.messages.DistributedMessageKind

object DistributedResult extends FromJsonCapable[DistributedResult]

case class DistributedResult(
  kind: DistributedMessageKind,
  result: JValue
) extends ToJsonCapable
