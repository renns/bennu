package com.qoid.bennu.model

import com.qoid.bennu.FromJsonCapable
import com.qoid.bennu.ToJsonCapable
import com.qoid.bennu.mapper.BennuMappedInstance
import com.qoid.bennu.mapper.BennuMapperCompanion
import com.qoid.bennu.model.id.AgentId
import com.qoid.bennu.model.id.InternalId
import m3.jdbc._
import net.liftweb.json._
import net.model3.chrono.DateTime

object Label extends BennuMapperCompanion[Label] with FromJsonCapable[Label]

case class Label(
  name: String,
  agentId: AgentId = AgentId(""),
  @PrimaryKey iid: InternalId = InternalId.random,
  data: JValue = JNothing,
  created: DateTime = new DateTime,
  modified: DateTime = new DateTime,
  createdByConnectionIid: InternalId = InternalId(""),
  modifiedByConnectionIid: InternalId = InternalId("")
) extends BennuMappedInstance[Label] with ToJsonCapable {

  override def copy2(
    agentId: AgentId = agentId,
    created: DateTime = created,
    modified: DateTime = modified,
    createdByConnectionIid: InternalId = createdByConnectionIid,
    modifiedByConnectionIid: InternalId = modifiedByConnectionIid
  ) = {
    copy(
      agentId = agentId,
      created = created,
      modified = modified,
      createdByConnectionIid = createdByConnectionIid,
      modifiedByConnectionIid = modifiedByConnectionIid
    )
  }
}
