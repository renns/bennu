package com.qoid.bennu.distributed

import com.qoid.bennu.JsonAssist._
import com.qoid.bennu.model.InternalId
import com.qoid.bennu.model.PeerId

case class DistributedRequest(
  fromPeerId: PeerId,
  toPeerId: PeerId,
  kind: DistributedRequestKind,
  data: JValue,
  iid: InternalId = InternalId.random
)
