package com.qoid.bennu.squery

import com.qoid.bennu.AgentView
import com.qoid.bennu.JdbcAssist
import com.qoid.bennu.MemoryCache
import com.qoid.bennu.MemoryListCache
import com.qoid.bennu.SecurityContext
import com.qoid.bennu.model.{Alias, AgentId, Handle, HasInternalId, InternalId}
import com.qoid.bennu.squery.ast.Evaluator
import com.qoid.bennu.squery.ast.Query
import m3.Txn
import m3.jdbc._
import m3.predef._

@com.google.inject.Singleton
class StandingQueryManager {
  private val cache = new MemoryCache[Handle, StandingQueryManager.CacheValue]
  private val agentTypeIndex = new MemoryListCache[(AgentId, String), Handle]

  def add(
    agentId: AgentId,
    aliasIid: InternalId,
    handle: Handle,
    tpe: String,
    query: Query,
    fn: (HasInternalId, StandingQueryAction) => Unit
  ): Unit = {
    cache.put(handle, StandingQueryManager.CacheValue(agentId, aliasIid, tpe.toLowerCase, query, fn))
    agentTypeIndex.put((agentId, tpe.toLowerCase), handle)
  }

  def remove(handle: Handle): Unit = {
    cache.get(handle).foreach { v =>
      val av = inject[AgentView]
      if (av.select[Alias](sql"iid = ${v.aliasIid}").length > 0) {
        agentTypeIndex.remove((v.agentId, v.tpe), handle)
        cache.remove(handle)
      }
    }
  }

  def notify[T <: HasInternalId](
    mapper: JdbcAssist.BennuMapperCompanion[T],
    instance: T,
    action: StandingQueryAction
  ): Unit = {
    for {
      handle <- agentTypeIndex.get((instance.agentId, mapper.typeName.toLowerCase))
      v <- cache.get(handle)
    } {
      Txn {
        Txn.setViaTypename[SecurityContext](SecurityContext.AliasSecurityContext(v.aliasIid))
        val av = inject[AgentView]

        if (Evaluator.evaluateQuery(av.constrict(mapper, v.query), instance) == Evaluator.VTrue) {
          v.fn(instance, action)
        }
      }
    }
  }
}

object StandingQueryManager {
  case class CacheValue(
    agentId: AgentId,
    aliasIid: InternalId,
    tpe: String,
    query: Query,
    fn: (HasInternalId, StandingQueryAction) => Unit
  )
}
