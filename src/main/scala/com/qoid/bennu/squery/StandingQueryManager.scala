package com.qoid.bennu.squery

import com.google.inject.Inject
import com.google.inject.Singleton
import com.qoid.bennu.JdbcAssist
import com.qoid.bennu.MemoryCache
import com.qoid.bennu.MemoryListCache
import com.qoid.bennu.model._
import com.qoid.bennu.security.AgentView
import com.qoid.bennu.security.AliasSecurityContext
import com.qoid.bennu.security.ConnectionSecurityContext
import com.qoid.bennu.security.SecurityContext
import com.qoid.bennu.squery.ast.Evaluator
import com.qoid.bennu.squery.ast.Query
import m3.Txn
import m3.jdbc._
import m3.predef._

@Singleton
class StandingQueryManager @Inject()(injector: ScalaInjector) {
  private val cache = new MemoryCache[Handle, StandingQueryManager.CacheValue]
  private val agentTypeIndex = new MemoryListCache[(AgentId, String), Handle]
  private val connectionIidCache = new MemoryCache[Handle, (List[InternalId], InternalId)]

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
    cache.get(handle).foreach { v =>
      val av = injector.instance[AgentView]

      val allowed = (v.remote, v.aliasIid, v.connectionIid, av.securityContext) match {
        case (false, Some(aliasIid), _, _) => av.hasAccessToAlias(aliasIid)
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
          case (false, Some(aliasIid), _) => Some(AliasSecurityContext(injector, aliasIid))
          case (true, _, Some(connectionIid)) => Some(ConnectionSecurityContext(injector, connectionIid))
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

  def addConnectionIids(handle: Handle, connectionIids: List[InternalId], aliasIid: InternalId): Unit = {
    connectionIidCache.put(handle, (connectionIids, aliasIid))
  }

  def removeConnectionIids(handle: Handle): Unit = {
    for ((_, aliasIid) <- connectionIidCache.get(handle)) {
      val av = injector.instance[AgentView]

      if (av.hasAccessToAlias(aliasIid)) {
        connectionIidCache.remove(handle)
      }
    }
  }

  def getConnectionIids(handle: Handle): List[InternalId] = {
    (for ((connectionIids, aliasIid) <- connectionIidCache.get(handle)) yield {
      val av = injector.instance[AgentView]

      if (av.hasAccessToAlias(aliasIid)) {
        connectionIids
      } else {
        Nil
      }
    }).getOrElse(Nil)
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
