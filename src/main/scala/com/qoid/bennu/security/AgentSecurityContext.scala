package com.qoid.bennu.security

import com.qoid.bennu.mapper.BennuMappedInstance
import com.qoid.bennu.mapper.BennuMapperCompanion
import com.qoid.bennu.model.Agent
import com.qoid.bennu.model.Alias
import com.qoid.bennu.model.id.AgentId
import com.qoid.bennu.model.id.InternalId
import com.qoid.bennu.query.ast.Query
import m3.Txn
import m3.jdbc._

object AgentSecurityContext {
  def apply[T](agentId: AgentId)(fn: => T): T = {
    Txn {
      Txn.setViaTypename[SecurityContext](new AgentSecurityContext(agentId))
      fn
    }
  }

  def apply[T](agentId: AgentId, connectionIid: InternalId)(fn: => T): T = {
    Txn {
      val connectionIidParm = connectionIid
      Txn.setViaTypename[SecurityContext](new AgentSecurityContext(agentId) {
        override val connectionIid: InternalId = connectionIidParm
      })
      fn
    }
  }
}

class AgentSecurityContext(
  override val agentId: AgentId
) extends SecurityContext {

  private lazy val agent = Agent.selectOne(sql"agentId = ${agentId}")
  private lazy val alias = Alias.fetch(agent.aliasIid)

  override def connectionIid: InternalId = alias.connectionIid
  override def aliasIid: InternalId = agent.aliasIid

  override def constrictQuery(mapper: BennuMapperCompanion[_], query: Query): Query = {
    query.and(Query.parse(sql"agentId = ${agentId}"))
  }

  override def canInsert[T <: BennuMappedInstance[T]](instance: T): Boolean = instance.agentId == agentId
  override def canUpdate[T <: BennuMappedInstance[T]](instance: T): Boolean = instance.agentId == agentId
  override def canDelete[T <: BennuMappedInstance[T]](instance: T): Boolean = instance.agentId == agentId

  override def canExportAgent: Boolean = true
}
