package com.qoid.bennu.webservices

import com.google.inject.Inject
import com.qoid.bennu.model.AgentId
import m3.servlet.beans.Parm
import com.qoid.bennu.model.Agent
import java.sql.Connection
import com.qoid.bennu.JsonAssist._

case class CreateAgent @Inject() (
  implicit
  conn: Connection,
  @Parm id: AgentId
) {
  
  def service = {
    Agent(
      iid = id.asIid, 
      name = id.value, 
      data = JNothing
    ).sqlInsert
    "successfully created the agent"
  }
  
}