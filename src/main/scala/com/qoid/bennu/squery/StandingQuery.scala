package com.qoid.bennu.squery

import com.qoid.bennu.model._
import m3.servlet.longpoll.ChannelId
import m3.json.Json
import com.qoid.bennu.JsonAssist._
import com.qoid.bennu.SecurityContext.AgentCapableSecurityContext
import com.qoid.bennu.squery.ast.Query
import m3.predef._
import m3.servlet.longpoll.ChannelManager
import m3.servlet.longpoll.Channel

object StandingQuery {

  case class TypeQuery(@Json("type") tpe: String, q: Option[String] = None) {
    val typeLc = tpe.toLowerCase()
    val query = q.map(qs=>Query.parse(qs)).getOrElse(Query.nil)
  }

  def defaultStandingQueryListener(channel: Channel, handle: InternalId, context: JValue): StandingQueryEvent => Unit = { 
    event: StandingQueryEvent =>
      val response = AsyncResponse(AsyncResponseType.SQuery, handle, true, event.toJson, context)
      channel.put(response.toJson)
  }

}

case class StandingQuery(
  agentId: AgentId,
  handle: InternalId,
  context: JValue,
  securityContext: AgentCapableSecurityContext,
  typeQueries: List[StandingQuery.TypeQuery],
  listener: StandingQueryEvent => Unit
) {
  val types = typeQueries.map(_.typeLc).toSet
  val typeQueriesByType = typeQueries.map(t => t.typeLc -> t).toMap
}
