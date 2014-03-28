package com.qoid.bennu.model.notification

import com.qoid.bennu.FromJsonCapable
import com.qoid.bennu.JsonAssist._
import com.qoid.bennu.ToJsonCapable
import com.qoid.bennu.model.id.InternalId

object VerificationRequest extends FromJsonCapable[VerificationRequest]

case class VerificationRequest(
  contentIid: InternalId,
  contentType: String,
  contentData: JValue,
  message: String
) extends ToJsonCapable
