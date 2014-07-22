package com.qoid.bennu.mapper

import com.qoid.bennu.model.id.AgentId
import com.qoid.bennu.model.id.InternalId
import net.model3.chrono.DateTime

trait BennuMappedInstance[T] {
  val agentId: AgentId
  val created: DateTime
  val modified: DateTime
  val createdByConnectionIid: InternalId
  val modifiedByConnectionIid: InternalId

  def copy2(
    agentId: AgentId = agentId,
    created: DateTime = created,
    modified: DateTime = modified,
    createdByConnectionIid: InternalId = createdByConnectionIid,
    modifiedByConnectionIid: InternalId = modifiedByConnectionIid
  ): T
}
