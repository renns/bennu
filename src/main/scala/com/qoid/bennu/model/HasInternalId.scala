package com.qoid.bennu.model

import com.qoid.bennu.JdbcAssist.BennuMapperCompanion
import com.qoid.bennu.JsonCapable
import com.qoid.bennu.squery._
import m3.predef._
import net.liftweb.json.JValue

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

  def notifyStandingQueries(action: StandingQueryAction): TInstance = {
    inject[StandingQueryManager].notify(mapper, cast, action)
    cast
  }
}
