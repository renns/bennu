package com.qoid.bennu.webservices

import com.google.inject.Inject
import com.qoid.bennu.SecurityContext.AgentCapableSecurityContext
import com.qoid.bennu.model._
import com.qoid.bennu.model.notification.IntroductionRequest
import java.sql.{ Connection => JdbcConn }
import m3.predef._
import m3.servlet.beans.Parm
import net.liftweb.json._
import scala.language.existentials

case class InitiateIntroductionService @Inject()(
  implicit conn: JdbcConn,
  securityContext: AgentCapableSecurityContext,
  @Parm aConnectionIid: InternalId,
  @Parm aMessage: String,
  @Parm bConnectionIid: InternalId,
  @Parm bMessage: String
) extends Logging {

  def service: JValue = {
    val aConnection = Connection.fetch(aConnectionIid)
    val bConnection = Connection.fetch(bConnectionIid)

    val introduction = Introduction(securityContext.agentId, aConnectionIid, IntroductionState.NotResponded, bConnectionIid, IntroductionState.NotResponded)
    introduction.sqlInsert

    val aIntroductionRequest = IntroductionRequest(introduction.iid, aMessage)
    val bIntroductionRequest = IntroductionRequest(introduction.iid, bMessage)

    Notification.sendNotification(aConnection.remotePeerId, NotificationKind.IntroductionRequest, aIntroductionRequest.toJson)
    Notification.sendNotification(bConnection.remotePeerId, NotificationKind.IntroductionRequest, bIntroductionRequest.toJson)

    JString("success")
  }
}
