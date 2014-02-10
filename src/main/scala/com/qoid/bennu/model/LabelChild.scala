package com.qoid.bennu.model

import com.qoid.bennu.JdbcAssist._
import m3.jdbc.PrimaryKey
import net.liftweb.json._

object LabelChild extends BennuMapperCompanion[LabelChild] {
}

case class LabelChild(
  @PrimaryKey iid: InternalId = InternalId.random,
  agentId: AgentId,
  parentIid: InternalId,
  childIid: InternalId,
  data: JValue = JNothing,
  deleted: Boolean = false
) extends HasInternalId with BennuMappedInstance[LabelChild] { self =>
  
  type TInstance = LabelChild
  
  def mapper = LabelChild
  
  override def copy2(
      iid: InternalId = self.iid, 
      agentId: AgentId = self.agentId, 
      data: JValue = self.data, 
      deleted: Boolean = self.deleted
  ) = {
    copy(iid = iid, agentId = agentId, data = data, deleted = deleted)
  }
}
