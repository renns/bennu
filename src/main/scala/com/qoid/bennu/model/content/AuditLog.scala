package com.qoid.bennu.model.content

import com.qoid.bennu.JsonAssist._
import com.qoid.bennu.ToJsonCapable
import com.qoid.bennu.distributed.DistributedMessageKind
import com.qoid.bennu.model.id.InternalId

case class AuditLog(
  kind: DistributedMessageKind,
  route: List[InternalId],
  messageData: JValue,
  success: Boolean,
  errorCode: Option[String]
) extends ToJsonCapable
