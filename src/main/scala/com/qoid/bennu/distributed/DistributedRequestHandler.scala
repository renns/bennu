package com.qoid.bennu.distributed

import com.qoid.bennu.model.Connection
import java.sql.{ Connection => JdbcConn }

trait DistributedRequestHandler {
  def handle(request: DistributedRequest, connection: Connection)(implicit jdbcConn: JdbcConn): DistributedResponse
}

object DistributedRequestHandler {
  def getHandler(kind: DistributedRequestKind): Option[DistributedRequestHandler] = {
    kind match {
      case DistributedRequestKind.GetProfile => Some(new GetProfileHandler)
      case DistributedRequestKind.Notification => Some(new NotificationHandler)
      case DistributedRequestKind.Query => Some(new QueryHandler)
      case k => None
    }
  }
}
