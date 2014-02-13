package com.qoid.bennu.webservices

import com.google.inject.Inject
import com.qoid.bennu.distributed.DistributedManager
import com.qoid.bennu.model._
import java.sql.{ Connection => JdbcConn }
import m3.servlet.beans.Parm
import net.liftweb.json._
import scala.concurrent.ExecutionContext.Implicits.global

case class SendNotification @Inject() (
  implicit
  jdbcConn: JdbcConn,
  distributedMgr: DistributedManager,
  @Parm connectionIid: InternalId,
  @Parm kind: NotificationKind,
  @Parm data: JValue
) {
  def service: JValue = {
    distributedMgr.sendNotification(connectionIid, kind, data)
    JString("success")
  }
}
