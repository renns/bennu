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
import com.qoid.bennu.model.LabeledContent
import com.qoid.bennu.model.Content
import com.qoid.bennu.model.LabelAcl
import com.qoid.bennu.model.LabelChild
import com.qoid.bennu.model
import m3.predef._
import com.qoid.bennu.JdbcAssist
import m3.jdbc._

case class CreateAgent @Inject() (
  implicit
  conn: Connection,
  @Parm id: AgentId,
  @Parm overWrite: Boolean = false
) {

  def service: JValue = {
    if ( overWrite ) doDelete
    doCreate
    ("agentId" -> id.value)
  }

  def doDelete = {
    JdbcAssist.allMappers.foreach { mapper =>
      conn.update(sql"delete from ${mapper.tableName.rawSql} where agentId = ${id.value}")
    }
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

