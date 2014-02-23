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


case class RegisterStandingQueryService2 @Inject()(
  sQueryMgr: StandingQueryManager,
  securityContext: AgentCapableSecurityContext,
  channelId: ChannelId,
  methodContext: Option[MethodInvocationContext],
  @Parm typeQueries: List[StandingQuery.TypeQuery],
  @Parm context: Option[JValue] = None 
) extends Logging {
  
  val resolvedContext = context.orElse(methodContext.map(_.value)).getOrElse(JNothing)
  
  def service: JObject = {
    val handle = InternalId.random
    sQueryMgr.add(
      StandingQuery(
        agentId = securityContext.agentId,
        channelId = channelId,
        handle = handle,
        securityContext = securityContext,
        context = resolvedContext,
        typeQueries = typeQueries
      )
    )
    ("handle" -> handle.value)
  }

}
