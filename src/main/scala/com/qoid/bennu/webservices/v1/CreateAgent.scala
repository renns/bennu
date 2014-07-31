package com.qoid.bennu.webservices.v1

import com.google.inject.Inject
import com.qoid.bennu.BennuException
import com.qoid.bennu.JsonAssist._
import com.qoid.bennu.JsonAssist.jsondsl._
import com.qoid.bennu.model.assist.AgentAssist
import com.qoid.bennu.model.assist.AliasAssist
import com.qoid.bennu.model.id.AgentId
import com.qoid.bennu.model.id.InternalId
import com.qoid.bennu.security.AgentSecurityContext
import m3.predef._
import m3.servlet.HttpResponseException
import m3.servlet.HttpStatusCodes
import m3.servlet.beans.Parm

case class CreateAgent @Inject()(
  agentAssist: AgentAssist,
  aliasAssist: AliasAssist,
  @Parm name: String,
  @Parm password: String
) extends Logging {

  def doPost(): JValue = {
    try {
      val agentId = AgentId.random
      val connectionIid = InternalId.random

      AgentSecurityContext(agentId, connectionIid) {
        val login = agentAssist.createAgent(name, password)
        val alias = aliasAssist.createAnonymousAlias(login.aliasIid)
        agentAssist.connectToIntroducer(alias.connectionIid)

        "authenticationId" -> login.authenticationId
      }
    } catch {
      case e: BennuException =>
        throw new HttpResponseException(HttpStatusCodes.BAD_REQUEST, e.getErrorCode()).initCause(e)
    }
  }
}
