package com.qoid.bennu.distributed

import com.qoid.bennu.JsonAssist._
import com.qoid.bennu.model.InternalId
import com.qoid.bennu.model.PeerId

case class DistributedResponse(
  distributedRequestIid: InternalId,
  fromPeerId: PeerId,
  toPeerId: PeerId,
  data: JValue,
  iid: InternalId = InternalId.random
)
