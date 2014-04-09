package com.qoid.bennu.webservices

import com.google.inject.Inject
import com.qoid.bennu.AgentManager
import com.qoid.bennu.JsonAssist._
import com.qoid.bennu.JsonAssist.jsondsl._
import m3.servlet.beans.Parm

case class CreateAgent @Inject() (
  agentMgr: AgentManager,
  @Parm name: String,
  @Parm password: String = "password" //TODO: Remove default value
) {

  def service: JValue = {
    val agent = agentMgr.createAgent(name, password)
    val anonymousAlias = agentMgr.createAnonymousAlias(agent.uberAliasIid)
    agentMgr.connectToIntroducer(anonymousAlias.iid)

    //TODO: Return uber alias' authenticationId instead of agent name
    "agentName" -> agent.name
  }
}
