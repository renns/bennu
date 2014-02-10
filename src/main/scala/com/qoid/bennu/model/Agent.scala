package com.qoid.bennu.model

import com.qoid.bennu.JdbcAssist._
import m3.jdbc.PrimaryKey
import net.liftweb.json._
import m3.LockFreeMap
import m3.servlet.longpoll.ChannelId

object Agent extends BennuMapperCompanion[Agent] {
}

case class Agent(
  @PrimaryKey iid: InternalId = InternalId.random,
  agentId: AgentId,
  name: String,
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
