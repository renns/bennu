package com.qoid.bennu.distributed.messages

import com.qoid.bennu.FromJsonCapable
import com.qoid.bennu.ToJsonCapable
import com.qoid.bennu.model.InternalId
import com.qoid.bennu.model.PeerId

object IntroductionConnect extends FromJsonCapable[IntroductionConnect]

case class IntroductionConnect(
  introductionIid: InternalId,
  localPeerId: PeerId,
  remotePeerId: PeerId
) extends ToJsonCapable
