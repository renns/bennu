package com.qoid.bennu.model

import com.qoid.bennu.JdbcAssist._
import com.qoid.bennu.JsonAssist._
import com.qoid.bennu.model.id._
import com.qoid.bennu.security.AgentView
import m3.jdbc.PrimaryKey
import m3.predef._
import net.model3.chrono.DateTime

object Agent extends BennuMapperCompanion[Agent] {
  override protected def preInsert(instance: Agent): Agent = {
    val av = inject[AgentView]

    av.insert[Alias](Alias(instance.name, iid = av.securityContext.aliasIid))

    instance
  }
}

case class Agent(
  name: String,
  agentId: AgentId = AgentId(""),
  uberAliasIid: InternalId = InternalId(""),
  @PrimaryKey iid: InternalId = InternalId.random,
  data: JValue = JNothing,
  created: DateTime = new DateTime,
  modified: DateTime = new DateTime,
  createdByAliasIid: InternalId = InternalId(""),
  modifiedByAliasIid: InternalId = InternalId("")
) extends HasInternalId with BennuMappedInstance[Agent] { self =>
  
  type TInstance = Agent
  
  def mapper = Agent

  override def copy2(
    iid: InternalId = self.iid,
    agentId: AgentId = self.agentId,
    data: JValue = self.data,
    created: DateTime = self.created,
    modified: DateTime = self.modified,
    createdByAliasIid: InternalId = self.createdByAliasIid,
    modifiedByAliasIid: InternalId = self.modifiedByAliasIid
  ) = {
    copy(
      iid = iid,
      agentId = agentId,
      data = data,
      created = created,
      modified = modified,
      createdByAliasIid = createdByAliasIid,
      modifiedByAliasIid = modifiedByAliasIid
    )
  }
}
