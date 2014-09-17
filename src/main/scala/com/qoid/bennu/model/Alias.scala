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
import m3.predef._
import net.model3.chrono.DateTime

object Alias extends BennuMapperCompanion[Alias] with FromJsonCapable[Alias] {
  override def insert(instance: Alias): Alias = {
    val instance2 = super.insert(instance)
    inject[AgentAclManager].invalidateAliases()
    instance2
  }

  override def delete(instance: Alias): Alias = {
    val instance2 = super.delete(instance)
    inject[AgentAclManager].invalidateAliases()
    instance2
  }
}

case class Alias(
  labelIid: InternalId,
  connectionIid: InternalId,
  agentId: AgentId = AgentId(""),
  @PrimaryKey iid: InternalId = InternalId.random,
  data: JValue = JNothing,
  created: DateTime = new DateTime,
  modified: DateTime = new DateTime,
  @transient createdByConnectionIid: InternalId = InternalId(""),
  @transient modifiedByConnectionIid: InternalId = InternalId("")
) extends BennuMappedInstance[Alias] with ToJsonCapable {

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
