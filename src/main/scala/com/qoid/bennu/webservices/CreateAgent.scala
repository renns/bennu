package com.qoid.bennu.webservices

import com.google.inject.Inject
import com.qoid.bennu.JsonAssist._
import com.qoid.bennu.JsonAssist.jsondsl._
import com.qoid.bennu.model._
import com.qoid.bennu.AgentManager
import com.qoid.bennu.JdbcAssist
import java.sql.{ Connection => JdbcConn }
import m3.jdbc._
import m3.predef._
import m3.servlet.beans.Parm

case class CreateAgent @Inject() (
  injector: ScalaInjector,
  agentMgr: AgentManager,
  @Parm name: String,
  @Parm password: String = "password", //TODO: Remove default value
  @Parm overWrite: Boolean = false
) {

  def service: JValue = {
    if ( overWrite ) doDelete()

    val agent = agentMgr.createAgent(name, password)
    val anonymousAlias = agentMgr.createAnonymousAlias(agent.uberAliasIid)
    agentMgr.connectToIntroducer(anonymousAlias.iid)

    //TODO: Return uber alias' authenticationId instead of agent name
    "agentName" -> agent.name
  }

  private def doDelete(): Unit = {
    implicit val jdbcConn = injector.instance[JdbcConn]

    Agent.selectOpt(sql"name = $name").foreach { agent =>
      JdbcAssist.allMappers.foreach { mapper =>
        jdbcConn.update(sql"delete from ${mapper.tableName.rawSql} where agentId = ${agent.agentId.value}")
      }
    }
  }
}
