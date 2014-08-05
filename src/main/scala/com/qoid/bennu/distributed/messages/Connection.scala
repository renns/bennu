package com.qoid.bennu.distributed.messages

import com.qoid.bennu.ToJsonCapable
import com.qoid.bennu.model.id.InternalId

case class DeleteConnectionRequest(connectionIid: InternalId) extends ToJsonCapable
case class DeleteConnectionResponse(connectionIid: InternalId) extends ToJsonCapable
