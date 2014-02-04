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

import jsondsl._

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
      data = JObject(Nil)
    ).sqlInsert
    
    val rootLabel = Label(
      iid = InternalId.random,
      agentId = id,
      name = "uber label",
      data = ("color" -> "white")
    ).sqlInsert

    val rootAlias = Alias(
      iid = InternalId.random,
      agentId = id,
      name = "uber alias",
      rootLabelIid = rootLabel.iid,
      data = ("name" -> "Uber Alias")
    ).sqlInsert
    
    agent
    
  }
  
}

