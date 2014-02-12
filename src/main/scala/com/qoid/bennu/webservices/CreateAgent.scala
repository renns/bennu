package com.qoid.bennu.webservices

import com.google.inject.Inject
import com.qoid.bennu.model.AgentId
import m3.servlet.beans.Parm
import com.qoid.bennu.model.Agent
import java.sql.{ Connection => JdbcConn }
import com.qoid.bennu.JsonAssist._
import com.qoid.bennu.model.Alias
import com.qoid.bennu.model.PeerId
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
import com.qoid.bennu.model.Connection

object CreateAgent {
  val introducerAgentId = AgentId("introducer")
}

case class CreateAgent @Inject() (
  implicit
  conn: JdbcConn,
  @Parm id: AgentId,
  @Parm overWrite: Boolean = false,
  @Parm connectToIntroducer: Boolean = true
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
      iid = id.asIid,
      agentId = id,
      name = id.value, 
      data = JObject(Nil)
    ).sqlInsert
    
    val rootLabel = Label(
      agentId = id,
      name = "uber label",
      data = ("color" -> "white")
    ).sqlInsert

    val rootAlias = Alias(
      agentId = id,
      name = "uber alias",
      rootLabelIid = rootLabel.iid,
      data = ("name" -> "Uber Alias")
    ).sqlInsert
    
    val introLabel = Label(
      iid = InternalId.random,
      agentId = id,
      name = "intro label",
      data = ("color" -> "white")
    ).sqlInsert

    val introAlias = Alias(
      iid = InternalId.random,
      agentId = id,
      name = "intro alias",
      rootLabelIid = rootLabel.iid,
      data = ("name" -> "Intro Alias")
    ).sqlInsert
    
    LabelChild(
      agentId = id,
      parentIid = rootLabel.iid,
      childIid = introLabel.iid
    ).sqlInsert
    
    if ( connectToIntroducer ) {
      
      val introducer = Agent.fetch(CreateAgent.introducerAgentId.asIid)
      
      val introducerAlias = Alias.selectOne(sql"""agentId = 'introducer' and name = 'intro alias'""")
      
      val left = Connection(
        agentId = introducer.agentId,
        aliasIid = introducerAlias.iid,
        localPeerId = PeerId.random,
        remotePeerId = PeerId.random
      ).sqlInsert
      
      val right = Connection(
        agentId = id,
        aliasIid = introAlias.iid,
        localPeerId = left.remotePeerId,
        remotePeerId = left.localPeerId
      ).sqlInsert
            
    }

    agent

  }

}

