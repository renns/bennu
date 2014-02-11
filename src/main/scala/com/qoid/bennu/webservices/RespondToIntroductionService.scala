package com.qoid.bennu.webservices

import com.google.inject.Inject
import com.qoid.bennu.SecurityContext.AgentCapableSecurityContext
import com.qoid.bennu.model._
import com.qoid.bennu.model.notification._
import java.sql.{ Connection => JdbcConn }
import m3.predef._
import m3.servlet.beans.Parm
import net.liftweb.json._
import scala.language.existentials

case class RespondToIntroductionService @Inject()(
  implicit conn: JdbcConn,
  securityContext: AgentCapableSecurityContext,
  @Parm notificationIid: InternalId,
  @Parm accepted: Boolean
) extends Logging {

  def service: JValue = {
    val notification = Notification.fetch(notificationIid)
    notification.copy(consumed = true).sqlUpdate

    val connection = Connection.fetch(notification.fromConnectionIid)

    val introductionRequest = IntroductionRequest.fromJson(notification.data)
    val introductionResponse = IntroductionResponse(introductionRequest.introductionIid, accepted)

    Notification.sendNotification(connection.remotePeerId, NotificationKind.IntroductionResponse, introductionResponse.toJson)

    JString("success")
  }
}
