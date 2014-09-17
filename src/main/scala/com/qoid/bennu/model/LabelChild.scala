package com.qoid.bennu.model

import com.qoid.bennu.FromJsonCapable
import com.qoid.bennu.JsonAssist._
import com.qoid.bennu.ToJsonCapable
import com.qoid.bennu.mapper.BennuMappedInstance
import com.qoid.bennu.mapper.BennuMapperCompanion
import com.qoid.bennu.model.id.AgentId
import com.qoid.bennu.model.id.InternalId
import com.qoid.bennu.security.AgentAclManager
import m3.jdbc.mapper.PrimaryKey
import m3.predef.inject
import net.model3.chrono.DateTime

object LabelChild extends BennuMapperCompanion[LabelChild] with FromJsonCapable[LabelChild] {
  override def insert(instance: LabelChild): LabelChild = {
    val instance2 = super.insert(instance)
    inject[AgentAclManager].invalidateLabels()
    instance2
  }

  override def delete(instance: LabelChild): LabelChild = {
    val instance2 = super.delete(instance)
    inject[AgentAclManager].invalidateLabels()
    instance2
  }
}

case class LabelChild(
  parentIid: InternalId,
  childIid: InternalId,
  agentId: AgentId = AgentId(""),
  @PrimaryKey iid: InternalId = InternalId.random,
  data: JValue = JNothing,
  created: DateTime = new DateTime,
  modified: DateTime = new DateTime,
  @transient createdByConnectionIid: InternalId = InternalId(""),
  @transient modifiedByConnectionIid: InternalId = InternalId("")
) extends BennuMappedInstance[LabelChild] with ToJsonCapable {

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
