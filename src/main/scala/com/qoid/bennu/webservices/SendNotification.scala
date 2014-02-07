package com.qoid.bennu.webservices

import com.google.inject.Inject
import com.qoid.bennu.model.PeerId
import net.liftweb.json.JValue
import m3.servlet.beans.Parm
import java.sql.{ Connection => JdbcConn }
import com.qoid.bennu.model.Notification

case class SendNotification @Inject() (
  @Parm toPeer: PeerId, 
  @Parm kind: String,
  @Parm data: JValue
)(
  implicit 
  jdbcConn: JdbcConn
) {
  
  def service = {
    Notification.sendNotification(toPeer, kind, data)
  }
  

}