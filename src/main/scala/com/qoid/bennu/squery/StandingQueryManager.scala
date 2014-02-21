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
    //TODO: What happens if there is a collision on handle?
  }

  def remove(agentId: AgentId, handle: InternalId): Unit = {
    for (sQueryMap <- map.get(agentId)) {
      sQueryMap.remove(handle)
    }
  }

  def get(agentId: AgentId, tpe: String): List[StandingQuery] = {
    val typeLowerCase = tpe.toLowerCase

    for (
      sQueryMap <- map.get(agentId).toList;
      sQuery <- sQueryMap.values
      if sQuery.types.contains(typeLowerCase)
    ) yield sQuery
  }
  
  def notify[T <: HasInternalId](
    mapper: JdbcAssist.BennuMapperCompanion[T],
    instance: T,
    action: StandingQueryAction
  ): Unit = {

    val sQueries = get(instance.agentId, mapper.typeName)

    for {
      sQuery <- sQueries
      sc <- ChannelMap.channelToSecurityContextMap.get(sQuery.channelId)
      if Evaluator.evaluateQuery(sc.createView.constrict(mapper, Query.nil), instance) == Evaluator.VTrue
    } {
      val event = StandingQueryEvent(action, mapper.typeName, instance.toJson)
      val response = AsyncResponse(AsyncResponseType.SQuery, sQuery.handle, true, event.toJson)
      val channel = channelMgr.channel(sQuery.channelId)
      channel.put(response.toJson)
    }
  }
}
