package com.qoid.bennu.model

import com.qoid.bennu.JdbcAssist._
import m3.jdbc.PrimaryKey
import net.liftweb.json._

object Agent extends BennuMapperCompanion[Agent] {
}

case class Agent(
  @PrimaryKey iid: InternalId = InternalId.random,
  uberAliasIid: InternalId,
  name: String,
  agentId: AgentId = AgentId(""),
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
