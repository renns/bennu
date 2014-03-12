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
import com.google.inject.Inject
import net.codingwell.scalaguice.InjectorExtensions.ScalaInjector
import com.qoid.bennu.SecurityContext.ConnectionSecurityContext

@com.google.inject.Singleton
class StandingQueryManager @Inject()(injector: ScalaInjector) {
  private val cache = new MemoryCache[Handle, StandingQueryManager.CacheValue]
  private val agentTypeIndex = new MemoryListCache[(AgentId, String), Handle]

  def addLocal(
    agentId: AgentId,
    aliasIid: InternalId,
    handle: Handle,
    tpe: String,
    query: String,
    fn: (HasInternalId, StandingQueryAction) => Unit
  ): Unit = {
    cache.put(handle, StandingQueryManager.CacheValue(
      agentId,
      Some(aliasIid),
      None,
      tpe.toLowerCase,
      Query.parse(query),
      fn,
      remote = false
    ))

    agentTypeIndex.put((agentId, tpe.toLowerCase), handle)
  }

  def addRemote(
    agentId: AgentId,
    connectionIid: InternalId,
    handle: Handle,
    tpe: String,
    query: String,
    fn: (HasInternalId, StandingQueryAction) => Unit
  ): Unit = {
    cache.put(handle, StandingQueryManager.CacheValue(
      agentId,
      None,
      Some(connectionIid),
      tpe.toLowerCase,
      Query.parse(query),
      fn,
      remote = true
    ))

    agentTypeIndex.put((agentId, tpe.toLowerCase), handle)
  }

  def remove(handle: Handle): Unit = {
    //TODO: Make sure proper security is set by caller (remote)
    cache.get(handle).foreach { v =>
      val av = injector.instance[AgentView]

      val allowed = (v.remote, v.aliasIid, v.connectionIid, av.securityContext) match {
        case (false, Some(aliasIid), _, _) => av.select[Alias](sql"iid = $aliasIid").length > 0
        case (true, _, Some(connectionIid), sc: ConnectionSecurityContext) => connectionIid == sc.connectionIid
        case _ => false
      }

      if (allowed) {
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
        val securityContext = (v.remote, v.aliasIid, v.connectionIid) match {
          case (false, Some(aliasIid), _) => Some(SecurityContext.AliasSecurityContext(aliasIid))
          case (true, _, Some(connectionIid)) => Some(SecurityContext.ConnectionSecurityContext(connectionIid))
          case _ => None
        }

        securityContext match {
          case Some(sc) =>
            Txn.setViaTypename[SecurityContext](sc)
            val av = injector.instance[AgentView]

            if (Evaluator.evaluateQuery(av.constrict(mapper, v.query), instance) == Evaluator.VTrue) {
              v.fn(instance, action)
            }
          case None =>
        }
      }
    }
  }
}

object StandingQueryManager {
  case class CacheValue(
    agentId: AgentId,
    aliasIid: Option[InternalId],
    connectionIid: Option[InternalId],
    tpe: String,
    query: Query,
    fn: (HasInternalId, StandingQueryAction) => Unit,
    remote: Boolean
  )
}
