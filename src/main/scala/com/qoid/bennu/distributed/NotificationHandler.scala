package com.qoid.bennu.distributed

import com.qoid.bennu.JsonAssist._
import com.qoid.bennu.ToJsonCapable
import com.qoid.bennu.model._
import com.qoid.bennu.squery.StandingQueryAction
import java.sql.{ Connection => JdbcConn }
import m3.predef._

object NotificationHandler {
  case class Request(kind: NotificationKind, data: JValue) extends ToJsonCapable
}

class NotificationHandler extends DistributedRequestHandler {
  def handle(dr: DistributedRequest, connection: Connection)(implicit jdbcConn: JdbcConn): DistributedResponse = {
    val request = dr.data.deserialize[NotificationHandler.Request]

    val notification = Notification(
      connection.agentId,
      false,
      connection.iid,
      request.kind,
      data = request.data
    )

    notification.sqlInsert.notifyStandingQueries(StandingQueryAction.Insert)
    inject[NotificationListener].fireNotification(notification)

    DistributedResponse(
      dr.iid,
      connection.localPeerId,
      connection.remotePeerId,
      JNothing
    )
  }
}
