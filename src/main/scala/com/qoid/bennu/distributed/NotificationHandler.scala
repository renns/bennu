package com.qoid.bennu.distributed

import com.qoid.bennu.JsonAssist._
import com.qoid.bennu.model.Connection
import com.qoid.bennu.model.Notification
import com.qoid.bennu.model.NotificationListener
import com.qoid.bennu.squery.StandingQueryAction
import java.sql.{ Connection => JdbcConn }
import m3.predef._

class NotificationHandler extends DistributedRequestHandler {
  def handle(request: DistributedRequest, connection: Connection)(implicit jdbcConn: JdbcConn): DistributedResponse = {
    val notificationRequest = NotificationRequest.fromJson(request.data)

    val notification = Notification(
      connection.agentId,
      false,
      connection.iid,
      notificationRequest.kind,
      data = notificationRequest.data
    )

    notification.sqlInsert.notifyStandingQueries(StandingQueryAction.Insert)
    inject[NotificationListener].fireNotification(notification)

    DistributedResponse(
      request.iid,
      connection.localPeerId,
      connection.remotePeerId,
      JNothing
    )
  }
}
