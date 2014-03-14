package com.qoid.bennu.distributed.handlers

import com.qoid.bennu.distributed.messages._
import com.qoid.bennu.model.Connection
import com.qoid.bennu.model.Notification
import com.qoid.bennu.model.NotificationKind
import com.qoid.bennu.squery.StandingQueryAction
import java.sql.{ Connection => JdbcConn }
import net.codingwell.scalaguice.InjectorExtensions.ScalaInjector

object IntroductionRequestHandler {
  def handle(connection: Connection, introductionRequest: IntroductionRequest, injector: ScalaInjector): Unit = {
    implicit val jdbcConn = injector.instance[JdbcConn]

    val notification = Notification(
      fromConnectionIid = connection.iid,
      kind = NotificationKind.IntroductionRequest,
      agentId = connection.agentId,
      data = introductionRequest.toJson
    )

    Notification.insert(notification).notifyStandingQueries(StandingQueryAction.Insert)
  }
}
