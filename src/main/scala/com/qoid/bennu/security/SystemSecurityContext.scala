package com.qoid.bennu.security

import com.qoid.bennu.mapper.BennuMappedInstance
import com.qoid.bennu.mapper.BennuMapperCompanion
import com.qoid.bennu.model.Agent
import com.qoid.bennu.model.Alias
import com.qoid.bennu.model.Connection
import com.qoid.bennu.model.LabelAcl
import com.qoid.bennu.model.id.AgentId
import com.qoid.bennu.model.id.InternalId
import com.qoid.bennu.query.ast.Query
import m3.Txn

object SystemSecurityContext {
  def apply[T](fn: => T): T = {
    Txn {
      Txn.setViaTypename[SecurityContext](new SystemSecurityContext)
      fn
    }
  }
}

class SystemSecurityContext extends SecurityContext {
  override val agentId: AgentId = AgentId("")
  override val connectionIid: InternalId = InternalId("")
  override val aliasIid: InternalId = InternalId("")

  override def constrictQuery(mapper: BennuMapperCompanion[_], query: Query): Query = {
    mapper match {
      case Agent => query
      case Alias => query
      case Connection => query
      case LabelAcl => query
      case _ => query.and(Query.parse("1 <> 1"))
    }
  }

  override def canInsert[T <: BennuMappedInstance[T]](instance: T): Boolean = false
  override def canUpdate[T <: BennuMappedInstance[T]](instance: T): Boolean = false
  override def canDelete[T <: BennuMappedInstance[T]](instance: T): Boolean = false

  override def isAgentAdmin: Boolean = false
}
