package com.qoid.bennu.squery

import com.qoid.bennu.model._
import m3.servlet.longpoll.ChannelId
import m3.json.Json
import com.qoid.bennu.JsonAssist._
import com.qoid.bennu.SecurityContext.AgentCapableSecurityContext
import com.qoid.bennu.squery.ast.Query

object StandingQuery {
  
  case class TypeQuery(@Json("type") tpe: String, q: Option[String] = None) {
    val typeLc = tpe.toLowerCase()
    val query = q.map(qs=>Query.parse(qs)).getOrElse(Query.nil)
  }
  
}

case class StandingQuery(
  agentId: AgentId,
  channelId: ChannelId,
  handle: InternalId,
  context: JValue,
  securityContext: AgentCapableSecurityContext,
  typeQueries: List[StandingQuery.TypeQuery]
) {
  val types = typeQueries.map(_.typeLc).toSet
  val typeQueriesByType = typeQueries.map(t => t.typeLc -> t).toMap
}
