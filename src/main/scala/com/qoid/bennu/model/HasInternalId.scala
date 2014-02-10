package com.qoid.bennu.model

import com.qoid.bennu.JsonCapable
import net.liftweb.json.JValue
import m3.jdbc.ColumnMapper
import m3.jdbc.Mapper
import com.qoid.bennu.JdbcAssist.BennuMapperCompanion


trait HasInternalId extends JsonCapable { self =>
  
  type TInstance <: HasInternalId
  
  val iid: InternalId
  val agentId: AgentId
  val data: JValue
  val deleted: Boolean
  
  def mapper: BennuMapperCompanion[TInstance]
  
  def cast: TInstance = this.asInstanceOf[TInstance]
  
  def copy2(
      iid: InternalId = self.iid, 
      agentId: AgentId = self.agentId, 
      data: JValue = self.data, 
      deleted: Boolean = self.deleted
  ): TInstance
  
}
