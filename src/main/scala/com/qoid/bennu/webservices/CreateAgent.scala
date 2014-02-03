package com.qoid.bennu.webservices

import com.google.inject.Inject
import com.qoid.bennu.model.AgentId
import m3.servlet.beans.Parm
import com.qoid.bennu.model.Agent
import java.sql.Connection
import com.qoid.bennu.JsonAssist._
import com.qoid.bennu.model.Alias
import com.qoid.bennu.model.InternalId
import com.qoid.bennu.model.Label
import com.qoid.bennu.model.Alias

case class CreateAgent @Inject() (
  implicit
  conn: Connection,
  @Parm id: AgentId
) {
  
  def service = {
    doCreate
    "successfully created the agent"
  }
  
  def doCreate: Agent = {
    
    val agent = Agent(
      iid = id.asIid ,
      agentId = id,
      name = id.value, 
      data = JNothing
    ).sqlInsert
    
    val rootLabel = Label(
      iid = InternalId.random,
      agentId = id,
      name = "uber label",
      data = JNothing
    ).sqlInsert

    val rootAlias = Alias(
      iid = InternalId.random,
      agentId = id,
      name = "uber alias",
      rootLabelIid = rootLabel.iid,
      data = JNothing
    ).sqlInsert
    
    agent
    
  }
  
}

