package com.qoid.bennu.model

import com.qoid.bennu.FromJsonCapable
import com.qoid.bennu.ToJsonCapable
import com.qoid.bennu.mapper.BennuMappedInstance
import com.qoid.bennu.mapper.BennuMapperCompanion
import com.qoid.bennu.model.id.AgentId
import com.qoid.bennu.model.id.InternalId
import com.qoid.bennu.model.id.PeerId
import com.qoid.bennu.security.AgentAclManager
import m3.jdbc._
import m3.predef.inject
import net.liftweb.json._
import net.model3.chrono.DateTime

object Connection extends BennuMapperCompanion[Connection] with FromJsonCapable[Connection] {
  override def insert(instance: Connection): Connection = {
    val instance2 = super.insert(instance)
    inject[AgentAclManager].invalidateConnections()
    instance2
  }

  override def delete(instance: Connection): Connection = {
    val instance2 = super.delete(instance)
    inject[AgentAclManager].invalidateConnections()
    instance2
  }
}

case class Connection(
  aliasIid: InternalId,
  localPeerId: PeerId,
  remotePeerId: PeerId,
  labelIid: InternalId,
  agentId: AgentId = AgentId(""),
  @PrimaryKey iid: InternalId = InternalId.random,
  data: JValue = JNothing,
  created: DateTime = new DateTime,
  modified: DateTime = new DateTime,
  createdByConnectionIid: InternalId = InternalId(""),
  modifiedByConnectionIid: InternalId = InternalId("")
) extends BennuMappedInstance[Connection] with ToJsonCapable {
  
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
