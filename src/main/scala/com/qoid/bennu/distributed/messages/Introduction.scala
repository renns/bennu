package com.qoid.bennu.distributed.messages

import com.qoid.bennu.ToJsonCapable
import com.qoid.bennu.model.id.InternalId
import com.qoid.bennu.model.id.PeerId

case class InitiateIntroductionRequest(aConnectionIid: InternalId, aMessage: String, bConnectionIid: InternalId, bMessage: String) extends ToJsonCapable
case class InitiateIntroductionResponse(introductionIid: InternalId) extends ToJsonCapable

case class IntroductionRequest(introductionIid: InternalId, message: String, connectionIid: InternalId) extends ToJsonCapable
case class IntroductionResponse(introductionIid: InternalId) extends ToJsonCapable

case class AcceptIntroductionRequest(notificationIid: InternalId) extends ToJsonCapable
case class AcceptIntroductionResponse(notificationIid: InternalId) extends ToJsonCapable

case class IntroductionConnect(localPeerId: PeerId, remotePeerId: PeerId) extends ToJsonCapable
