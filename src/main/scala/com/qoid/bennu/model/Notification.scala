package com.qoid.bennu.model

import com.qoid.bennu.JdbcAssist._
import com.qoid.bennu.JsonAssist._
import com.qoid.bennu.model.id._
import com.qoid.bennu.model.notification.NotificationKind
import m3.jdbc._
import net.model3.chrono.DateTime

object Notification extends BennuMapperCompanion[Notification] {
}

case class Notification(
  fromConnectionIid: InternalId,
  kind: NotificationKind,
  consumed: Boolean = false,
  agentId: AgentId = AgentId(""),
  @PrimaryKey iid: InternalId = InternalId.random,
  data: JValue = JNothing,
  created: DateTime = new DateTime,
  modified: DateTime = new DateTime,
  createdByConnectionIid: InternalId = InternalId(""),
  modifiedByConnectionIid: InternalId = InternalId("")
) extends HasInternalId with BennuMappedInstance[Notification] {
  self =>
  
  type TInstance = Notification
  
  def mapper = Notification

  override def copy2(
    iid: InternalId = self.iid,
    agentId: AgentId = self.agentId,
    data: JValue = self.data,
    created: DateTime = self.created,
    modified: DateTime = self.modified,
    createdByConnectionIid: InternalId = self.createdByConnectionIid,
    modifiedByConnectionIid: InternalId = self.modifiedByConnectionIid
  ) = {
    copy(
      iid = iid,
      agentId = agentId,
      data = data,
      created = created,
      modified = modified,
      createdByConnectionIid = createdByConnectionIid,
      modifiedByConnectionIid = modifiedByConnectionIid
    )
  }
}
