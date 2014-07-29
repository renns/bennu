package com.qoid.bennu.distributed

import com.qoid.bennu.FromJsonCapable
import com.qoid.bennu.JsonAssist._
import com.qoid.bennu.ToJsonCapable

object DistributedResult extends FromJsonCapable[DistributedResult]

case class DistributedResult(
  kind: DistributedMessageKind,
  result: JValue
) extends ToJsonCapable
