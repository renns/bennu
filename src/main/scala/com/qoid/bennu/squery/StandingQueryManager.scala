package com.qoid.bennu.squery

import com.qoid.bennu.model._
import m3.LockFreeMap

@com.google.inject.Singleton
class StandingQueryManager {
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
      if sQuery.tpe.toLowerCase == typeLowerCase
    ) yield sQuery
  }
}
