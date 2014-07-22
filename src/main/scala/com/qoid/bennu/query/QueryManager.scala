package com.qoid.bennu.query

import com.google.inject.Inject
import com.google.inject.Singleton
import com.qoid.bennu.JsonAssist._
import com.qoid.bennu.ToJsonCapable
import com.qoid.bennu.distributed.messages.QueryResponse
import com.qoid.bennu.distributed.messages.StandingQueryResponse
import com.qoid.bennu.mapper.MapperAssist
import com.qoid.bennu.model.id.AgentId
import com.qoid.bennu.model.id.DistributedMessageId
import com.qoid.bennu.model.id.InternalId
import com.qoid.bennu.query.ast.Evaluator
import com.qoid.bennu.query.ast.Query
import com.qoid.bennu.security.SecurityContext
import com.qoid.bennu.session.SessionManager
import m3.LockFreeMap
import m3.json.Json
import m3.predef._
import m3.servlet.longpoll.ChannelId

@Singleton
class QueryManager @Inject()(
  injector: ScalaInjector,
  sessionMgr: SessionManager,
  standingQueryRepo: StandingQueryRepository
) {

  private val queries = LockFreeMap.empty[(AgentId, DistributedMessageId, List[InternalId]), (ChannelId, JValue, Boolean)]

  def addQuery(
    messageId: DistributedMessageId,
    route: List[InternalId],
    channelId: ChannelId,
    context: JValue,
    standing: Boolean
  ): Unit = {

    queries.put((agentId, messageId, route), (channelId, context, standing))
  }

  def cancelQuery(messageId: DistributedMessageId, route: List[InternalId]): Unit = {
    queries.remove((agentId, messageId, route))

    //TODO: send cancel query message
  }

  def handleQueryResponse(
    messageId: DistributedMessageId,
    route: List[InternalId],
    queryResponse: QueryResponse
  ): Unit = {

    queries.get((agentId, messageId, route)) match {
      case Some((channelId, context, standing)) =>

        sessionMgr.getSessionOpt(channelId) match {
          case Some(session) =>
            val response = QueryManager.QueryResponse(
              QueryResponseType.Query,
              queryResponse.tpe,
              context,
              queryResponse.results,
              route
            )

            session.channel.put(response.toJson)

            if (!standing) {
              queries.remove((agentId, messageId, route))
            }

          case _ =>
            if (standing) {
              cancelQuery(messageId, route)
            } else {
              queries.remove((agentId, messageId, route))
            }
        }

      case _ => cancelQuery(messageId, route)
    }
  }

  def handleStandingQueryResponse(
    messageId: DistributedMessageId,
    route: List[InternalId],
    standingQueryResponse: StandingQueryResponse
  ): Unit = {

    queries.get((agentId, messageId, route)) match {
      case Some((channelId, context, standing)) =>

        sessionMgr.getSessionOpt(channelId) match {
          case Some(session) =>
            val response = QueryManager.QueryResponse(
              QueryResponseType.SQuery,
              standingQueryResponse.tpe,
              context,
              standingQueryResponse.results,
              route,
              Some(standingQueryResponse.action)
            )

            session.channel.put(response.toJson)

          case _ => cancelQuery(messageId, route)
        }

      case _ => cancelQuery(messageId, route)
    }
  }

  def notifyStandingQueries[T : Manifest](
    instance: T,
    action: StandingQueryAction
  ): Unit = {
    val mapper = MapperAssist.findMapperByType[T]

    standingQueryRepo.find(agentId, mapper.typeName).foreach { sq =>
      //TODO: set security context
      val securityContext = injector.instance[SecurityContext]

      if (Evaluator.evaluateQuery(securityContext.constrictQuery(mapper, Query.parse(sq.query)), instance) == Evaluator.VTrue) {
        //TODO: send standing query response
      }
    }
  }

  private def agentId: AgentId = injector.instance[SecurityContext].agentId
}

object QueryManager {
  case class QueryResponse(
    responseType: QueryResponseType,
    @Json("type") tpe: String,
    context: JValue,
    results: JValue,
    route: List[InternalId],
    action: Option[StandingQueryAction] = None
  ) extends ToJsonCapable
}
