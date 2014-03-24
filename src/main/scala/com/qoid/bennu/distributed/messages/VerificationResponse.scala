package com.qoid.bennu.distributed.messages

import com.qoid.bennu.FromJsonCapable
import com.qoid.bennu.ToJsonCapable
import com.qoid.bennu.model.id.InternalId
import com.qoid.bennu.model.id.SharedId

object VerificationResponse extends FromJsonCapable[VerificationResponse]

case class VerificationResponse(
  contentIid: InternalId,
  verificationContentIid: InternalId,
  verifierId: SharedId
) extends ToJsonCapable
