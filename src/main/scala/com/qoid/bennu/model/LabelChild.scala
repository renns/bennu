package com.qoid.bennu.model

import com.qoid.bennu.JdbcAssist._
import com.qoid.bennu.model.id._
import m3.jdbc.PrimaryKey
import net.liftweb.json._

object LabelChild extends BennuMapperCompanion[LabelChild] {
  val parentIidAttrName = "com.qoid.bennu.model.LabelChild.parentIid"
}

case class LabelChild(
  parentIid: InternalId,
  childIid: InternalId,
  agentId: AgentId = AgentId(""),
  @PrimaryKey iid: InternalId = InternalId.random,
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
