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
import m3.servlet.beans.MultiRequestHandler.MethodInvocationContext
import m3.servlet.longpoll.Channel


case class RegisterStandingQueryService @Inject()(
  sQueryMgr: StandingQueryManager,
  securityContext: AgentCapableSecurityContext,
  channel: Channel,
  context: Option[MethodInvocationContext],
  @Parm types: List[String]
) extends Logging {
  
  def service: JObject = {
    val handle = InternalId.random
    val resolvedContext = context.map(_.value).getOrElse(JNothing)
    sQueryMgr.add(
      StandingQuery(
        agentId = securityContext.agentId,
        handle = handle,
        context = resolvedContext,
        securityContext = securityContext,
        typeQueries = types.map( t => StandingQuery.TypeQuery(t.toLowerCase) ),
        listener = StandingQuery.defaultStandingQueryListener(channel, handle, resolvedContext)
      )
    )
    ("handle" -> handle.value)
  }

}
