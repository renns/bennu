package com.qoid.bennu.model.notification

import com.qoid.bennu.FromJsonCapable
import com.qoid.bennu.ToJsonCapable
import com.qoid.bennu.model.InternalId

object IntroductionResponse extends FromJsonCapable[IntroductionResponse]

case class IntroductionResponse(
  introductionIid: InternalId,
  accepted: Boolean
) extends ToJsonCapable
