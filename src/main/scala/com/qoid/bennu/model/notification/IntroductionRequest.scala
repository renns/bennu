package com.qoid.bennu.model.notification

import com.qoid.bennu.FromJsonCapable
import com.qoid.bennu.JsonAssist._
import com.qoid.bennu.ToJsonCapable
import com.qoid.bennu.model.InternalId

object IntroductionRequest extends FromJsonCapable[IntroductionRequest]

case class IntroductionRequest(
  introductionIid: InternalId,
  message: String,
  profile: JValue
) extends ToJsonCapable
