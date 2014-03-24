package com.qoid.bennu.distributed.messages

import com.qoid.bennu.FromJsonCapable
import com.qoid.bennu.ToJsonCapable
import com.qoid.bennu.model.id.InternalId

object VerificationRequest extends FromJsonCapable[VerificationRequest]

case class VerificationRequest(
  contentIid: InternalId,
  message: String
) extends ToJsonCapable
