package com.qoid.bennu.model

import com.qoid.bennu.JdbcAssist._
import com.qoid.bennu.model.id._
import m3.jdbc._
import net.liftweb.json._
import com.qoid.bennu.model.notification.NotificationKind

object Notification extends BennuMapperCompanion[Notification] {
}

case class Notification(
  fromConnectionIid: InternalId,
  kind: NotificationKind,
  consumed: Boolean = false,
  agentId: AgentId = AgentId(""),
  @PrimaryKey iid: InternalId = InternalId.random,
  data: JValue = JNothing,
  deleted: Boolean = false
) extends HasInternalId with BennuMappedInstance[Notification] {
  self =>
  
  type TInstance = Notification
  
  def mapper = Notification
  
  override def copy2(
      iid: InternalId = self.iid, 
      agentId: AgentId = self.agentId, 
      data: JValue = self.data, 
      deleted: Boolean = self.deleted
  ) = {
    copy(iid = iid, agentId = agentId, data = data, deleted = deleted)
  }
}
