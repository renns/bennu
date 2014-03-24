package com.qoid.bennu.model

import com.qoid.bennu.JdbcAssist.BennuMapperCompanion
import com.qoid.bennu.ToJsonCapable
import com.qoid.bennu.model.id.AgentId
import com.qoid.bennu.model.id.InternalId
import com.qoid.bennu.squery._
import m3.predef._
import net.liftweb.json.JValue
import net.model3.transaction.Transaction

trait HasInternalId extends ToJsonCapable { self =>
  
  type TInstance <: HasInternalId
  
  val iid: InternalId
  val agentId: AgentId
  val data: JValue
  val deleted: Boolean
  
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
      deleted: Boolean = self.deleted
  ): TInstance

  def notifyStandingQueries(action: StandingQueryAction): Unit = {
    inject[Transaction].events.addListener(new Transaction.Adapter {
        override def commit(txn: Transaction) = {
          SqueryEvalThread.enqueue(action, self)
        }
    })
  }
}
