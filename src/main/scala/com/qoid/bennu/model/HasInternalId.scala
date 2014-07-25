package com.qoid.bennu.model

import com.qoid.bennu.JdbcAssist.BennuMapperCompanion
import com.qoid.bennu.ToJsonCapable
import com.qoid.bennu.model.id.AgentId
import com.qoid.bennu.model.id.InternalId
import m3.json.LiftJsonAssist._
import net.model3.chrono.DateTime

trait HasInternalId extends ToJsonCapable { self =>
  
  type TInstance <: HasInternalId
  
  val iid: InternalId
  val agentId: AgentId
  val data: JValue
  val created: DateTime
  val modified: DateTime
  val createdByAliasIid: InternalId
  val modifiedByAliasIid: InternalId
  
  def mapper: BennuMapperCompanion[TInstance]
  
  /**
   * A safeCast to it's type (one asInstanceOf to rule them all)
   * 
   * Had some type challenges using a self type of TInstance so settled on having this single safeCast to get the same effect
   * 
   */
  def safeCast: TInstance = this.asInstanceOf[TInstance]
  
  def copy2(
      iid: InternalId = self.iid, 
      agentId: AgentId = self.agentId, 
      data: JValue = self.data,
      created: DateTime = self.created,
      modified: DateTime = self.modified,
      createdByAliasIid: InternalId = self.createdByAliasIid,
      modifiedByAliasIid: InternalId = self.modifiedByAliasIid
  ): TInstance
}
