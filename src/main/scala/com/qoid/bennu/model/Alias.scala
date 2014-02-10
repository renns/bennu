package com.qoid.bennu.model

import com.qoid.bennu.JdbcAssist._
import m3.jdbc.PrimaryKey
import net.liftweb.json._

object Alias extends BennuMapperCompanion[Alias] {
}

case class Alias(
  @PrimaryKey iid: InternalId = InternalId.random,
  agentId: AgentId,
  rootLabelIid: InternalId,
  name: String,
  data: JValue = JNothing,
  deleted: Boolean = false
) extends HasInternalId with BennuMappedInstance[Alias] { self =>
  
  type TInstance = Alias
  
  def mapper = Alias
  
  override def copy2(
      iid: InternalId = self.iid, 
      agentId: AgentId = self.agentId, 
      data: JValue = self.data, 
      deleted: Boolean = self.deleted
  ) = {
    copy(iid = iid, agentId = agentId, data = data, deleted = deleted)
  }
  
}
