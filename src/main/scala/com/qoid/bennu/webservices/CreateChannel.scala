package com.qoid.bennu.webservices

import com.google.inject.Inject
import com.qoid.bennu.model.AgentId
import m3.servlet.beans.Parm
import m3.servlet.longpoll.ChannelManager
import com.qoid.bennu.model.Agent
import java.sql.Connection
import com.qoid.bennu.JsonAssist._
import com.qoid.bennu.SecurityContext

case class CreateChannel @Inject() (
  implicit 
  conn: Connection,
  manager: ChannelManager,
  @Parm agentId: AgentId
) {
  
  def service = {
//    val agent = Agent.fetch(agentId.asIid)
    val channel = manager.createChannel()
    Agent.channelToAgentIdMap += channel.id -> agentId
    jobj("id", JString(channel.id.value))
  }
  
}
