package com.qoid.bennu.squery

import com.qoid.bennu.model._
import m3.LockFreeMap
import com.qoid.bennu.JdbcAssist
import com.qoid.bennu.security.ChannelMap
import com.qoid.bennu.squery.ast.Evaluator
import com.qoid.bennu.squery.ast.Query
import m3.servlet.longpoll.ChannelManager
import com.google.inject.Inject
import m3.Txn
import com.qoid.bennu.SecurityContext
import m3.predef._
import com.qoid.bennu.AgentView

@com.google.inject.Singleton
class StandingQueryManager @Inject() (channelMgr: ChannelManager) {
  
  private val map = new LockFreeMap[AgentId, LockFreeMap[InternalId, StandingQuery]]

  def add(sQuery: StandingQuery): Unit = {
    val sQueryMap = map.getOrElseUpdate(sQuery.agentId, new LockFreeMap[InternalId, StandingQuery])
    sQueryMap += sQuery.handle -> sQuery
  }

  def remove(agentId: AgentId, handle: InternalId): Unit = {
    for (sQueryMap <- map.get(agentId)) {
      sQueryMap.remove(handle)
    }
  }

  def get(agentId: AgentId, tpe: String): List[(StandingQuery,StandingQuery.TypeQuery)] = {
    val typeLowerCase = tpe.toLowerCase

    for (
      sQueryMap <- map.get(agentId).toList;
      sQuery <- sQueryMap.values;
      tQuery <- sQuery.typeQueriesByType.get(typeLowerCase)
    ) yield sQuery -> tQuery
  }
  
  def notify[T <: HasInternalId](
    mapper: JdbcAssist.BennuMapperCompanion[T],
    instance: T,
    action: StandingQueryAction
  ): Unit = {
    val event = StandingQueryEvent(action, mapper.typeName, instance.toJson)
    val queries = get(instance.agentId, mapper.typeName)
    for {
      q <- queries
    } {
      Txn {
        Txn.setViaTypename[SecurityContext](q._1.securityContext)
        val av = inject[AgentView]
        if ( Evaluator.evaluateQuery(av.constrict(mapper, q._2.query), instance) == Evaluator.VTrue ) {
          val response = AsyncResponse(AsyncResponseType.SQuery, q._1.handle, true, event.toJson, q._1.context)
          q._1.listener(event)
        }
      }
    }
  }
}
