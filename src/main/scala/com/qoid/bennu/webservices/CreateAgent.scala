package com.qoid.bennu.webservices

import com.google.inject.Inject
import com.qoid.bennu.JdbcAssist
import com.qoid.bennu.JsonAssist._
import com.qoid.bennu.model.Agent
import com.qoid.bennu.model.AgentId
import com.qoid.bennu.model.Alias
import com.qoid.bennu.model.Connection
import com.qoid.bennu.model.Label
import com.qoid.bennu.model.LabelChild
import com.qoid.bennu.model.PeerId
import java.sql.{ Connection => JdbcConn }
import jsondsl._
import m3.jdbc._
import m3.servlet.beans.Parm

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

    val rootLabel = Label(
      agentId = id,
      name = "uber label",
      data = ("color" -> "white")
    ).sqlInsert
    
    val rootAlias = Alias(
      agentId = id,
      profile = JObject(List(JField("name", "Uber Alias"), JField("imgSrc", ""))),
      rootLabelIid = rootLabel.iid
    ).sqlInsert

    val agent = Agent(
      iid = id.asIid,
      uberAliasIid = rootAlias.iid,
      agentId = id,
      name = id.value, 
      data = JObject(Nil)
    ).sqlInsert
    
    val introLabel = Label(
      agentId = id,
      name = "intro label",
      data = ("color" -> "white")
    ).sqlInsert

    val introAlias = Alias(
      agentId = id,
      profile = JObject(List(JField("name", "Intro Alias"), JField("imgSrc", ""))),
      rootLabelIid = rootLabel.iid
    ).sqlInsert
    
    LabelChild(
      agentId = id,
      parentIid = rootLabel.iid,
      childIid = introLabel.iid
    ).sqlInsert
    
    if ( connectToIntroducer ) {
      
      val introducer = Agent.fetch(CreateAgent.introducerAgentId.asIid)
      
      val introducerAlias = Alias.selectOne(sql"""agentId = 'introducer' and json_str(profile, 'name') = 'Intro Alias'""")

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

