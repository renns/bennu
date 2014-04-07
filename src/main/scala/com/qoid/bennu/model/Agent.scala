package com.qoid.bennu.model

import com.qoid.bennu.JdbcAssist._
import com.qoid.bennu.JsonAssist._
import com.qoid.bennu.model.id._
import com.qoid.bennu.security.AgentView
import m3.jdbc.PrimaryKey
import m3.predef._

object Agent extends BennuMapperCompanion[Agent] {
  override protected def preInsert(instance: Agent): Agent = {
    val av = inject[AgentView]

    val alias = av.insert[Alias](Alias(instance.name))

    instance.copy(uberAliasIid = alias.iid)
  }
}

case class Agent(
  name: String,
  agentId: AgentId = AgentId(""),
  uberAliasIid: InternalId = InternalId(""),
  @PrimaryKey iid: InternalId = InternalId.random,
  data: JValue = JNothing,
  deleted: Boolean = false
) extends HasInternalId with BennuMappedInstance[Agent] { self =>
  
  type TInstance = Agent
  
  def mapper = Agent

  override def copy2(
      iid: InternalId = self.iid, 
      agentId: AgentId = self.agentId, 
      data: JValue = self.data, 
      deleted: Boolean = self.deleted
  ) = {
    copy(iid = iid, agentId = agentId, data = data, deleted = deleted)
  }
  
}
