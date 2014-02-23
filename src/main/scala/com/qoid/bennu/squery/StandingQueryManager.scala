package com.qoid.bennu.squery

import com.qoid.bennu.model._
import m3.LockFreeMap
import com.qoid.bennu.JdbcAssist
import com.qoid.bennu.security.ChannelMap
import com.qoid.bennu.squery.ast.Evaluator
import com.qoid.bennu.squery.ast.Query
import m3.servlet.longpoll.ChannelManager
import com.google.inject.Inject

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
      val av = q._1.securityContext.createView
      if ( Evaluator.evaluateQuery(av.constrict(mapper, q._2.query), instance) == Evaluator.VTrue ) {
        val response = AsyncResponse(AsyncResponseType.SQuery, q._1.handle, true, event.toJson)
        val channel = channelMgr.channel(q._1.channelId)
        channel.put(response.toJson)
      }
    }
  }
}
