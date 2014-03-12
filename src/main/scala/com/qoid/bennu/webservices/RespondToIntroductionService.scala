package com.qoid.bennu.webservices

import com.google.inject.Inject
import com.qoid.bennu.SecurityContext.AgentCapableSecurityContext
import com.qoid.bennu.distributed.DistributedManager
import com.qoid.bennu.model._
import com.qoid.bennu.model.notification.IntroductionRequest
import com.qoid.bennu.model.notification.IntroductionResponse
import com.qoid.bennu.squery._
import java.sql.{ Connection => JdbcConn }
import m3.predef._
import m3.servlet.beans.Parm
import net.liftweb.json._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.language.existentials

case class RespondToIntroductionService @Inject()(
  implicit conn: JdbcConn,
  securityContext: AgentCapableSecurityContext,
  sQueryMgr: StandingQueryManager,
  distributedMgr: DistributedManager,
  @Parm notificationIid: InternalId,
  @Parm accepted: Boolean
) extends Logging {

  def service: JValue = {
//    val notification = Notification.fetch(notificationIid)
//    notification.copy(consumed = true).sqlUpdate.notifyStandingQueries(StandingQueryAction.Update)
//
//    val connection = Connection.fetch(notification.fromConnectionIid)
//
//    val introductionRequest = IntroductionRequest.fromJson(notification.data)
//    val introductionResponse = IntroductionResponse(introductionRequest.introductionIid, accepted)
//
//    distributedMgr.sendNotification(connection.iid, NotificationKind.IntroductionResponse, introductionResponse.toJson)

    JString("success")
  }
}
