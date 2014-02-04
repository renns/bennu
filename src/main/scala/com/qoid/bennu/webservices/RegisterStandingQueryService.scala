package com.qoid.bennu.webservices

import com.google.inject.Inject
import com.qoid.bennu.SecurityContext.AgentCapableSecurityContext
import com.qoid.bennu.model._
import com.qoid.bennu.squery._
import m3.predef._
import m3.servlet.beans.Parm
import m3.servlet.longpoll.ChannelId
import scala.language.existentials
import com.qoid.bennu.JsonAssist._
import jsondsl._


case class RegisterStandingQueryService @Inject()(
  sQueryMgr: StandingQueryManager,
  securityContext: AgentCapableSecurityContext,
  channelId: ChannelId,
  @Parm types: List[String]
) extends Logging {
  
  def service: JObject = {
    val handle = InternalId.random
    sQueryMgr.add(
      StandingQuery(securityContext.agentId, channelId, handle, types.map(_.toLowerCase).toSet)
    )
    ("handle" -> handle.value)
  }

}
