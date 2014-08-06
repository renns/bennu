package com.qoid.bennu.security

import com.qoid.bennu.mapper.BennuMappedInstance
import com.qoid.bennu.mapper.BennuMapperCompanion
import com.qoid.bennu.model.id.AgentId
import com.qoid.bennu.model.id.InternalId
import com.qoid.bennu.query.ast.Query

trait SecurityContext {
  def agentId: AgentId
  def connectionIid: InternalId
  def aliasIid: InternalId

  def constrictQuery(mapper: BennuMapperCompanion[_], query: Query): Query

  def canInsert[T <: BennuMappedInstance[T]](instance: T): Boolean
  def canUpdate[T <: BennuMappedInstance[T]](instance: T): Boolean
  def canDelete[T <: BennuMappedInstance[T]](instance: T): Boolean

  def canExportAgent = false
  def canSpawnSession = false
}
