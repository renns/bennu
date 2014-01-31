package com.qoid.bennu.webservices

import com.google.inject.Inject
import com.qoid.bennu.SecurityContext.AgentCapableSecurityContext
import com.qoid.bennu.model._
import com.qoid.bennu.squery._
import m3.predef._
import m3.servlet.beans.Parm
import m3.servlet.longpoll.ChannelId
import net.liftweb.json._
import scala.language.existentials

case class RegisterStandingQueryService @Inject()(
  sQueryMgr: StandingQueryManager,
  securityContext: AgentCapableSecurityContext,
  channelId: ChannelId,
  @Parm types: List[String],
  @Parm handle: InternalId
) extends Logging {

  def service: JValue = {
    types.foreach( tpe =>
      sQueryMgr.add(
        StandingQuery(securityContext.agentId, channelId, handle, tpe)
      )
    )
    JString("success")
  }
}
