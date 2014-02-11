package com.qoid.bennu.webservices

import com.google.inject.Inject
import com.qoid.bennu.model._
import java.sql.{ Connection => JdbcConn }
import m3.servlet.beans.Parm
import net.liftweb.json._

case class SendNotification @Inject() (
  @Parm toPeer: PeerId,
  @Parm kind: NotificationKind,
  @Parm data: JValue
)(
  implicit 
  jdbcConn: JdbcConn
) {
  def service: JValue = {
    Notification.sendNotification(toPeer, kind, data)
    JString("success")
  }
}
