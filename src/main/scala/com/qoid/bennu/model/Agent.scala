package com.qoid.bennu.model

import com.qoid.bennu.JdbcAssist._
import m3.jdbc.PrimaryKey
import net.liftweb.json._
import m3.LockFreeMap
import m3.servlet.longpoll.ChannelId

object Agent extends BennuMapperCompanion[Agent] {
  
  val channelToAgentIdMap = LockFreeMap[ChannelId,AgentId]()
  
}

case class Agent(
  @PrimaryKey iid: InternalId,
  name: String,
  data: JValue,
  deleted: Boolean = false
) extends HasInternalId with BennuMappedInstance[Agent] {
  lazy val agentId: AgentId = AgentId(iid.value)
  def mapper = Agent
}
