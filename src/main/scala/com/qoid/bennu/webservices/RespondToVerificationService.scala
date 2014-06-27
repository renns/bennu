package com.qoid.bennu.webservices

import com.google.inject.Inject
import com.qoid.bennu.JsonAssist._
import com.qoid.bennu.distributed.DistributedManager
import com.qoid.bennu.model._
import com.qoid.bennu.model.id.InternalId
import com.qoid.bennu.model.notification.VerificationRequest
import com.qoid.bennu.security.AgentView
import com.qoid.bennu.security.SecurityContext
import java.sql.{ Connection => JdbcConn }
import m3.predef._
import m3.servlet.beans.Parm

case class RespondToVerificationService @Inject()(
  injector: ScalaInjector,
  distributedMgr: DistributedManager,
  securityContext: SecurityContext,
  @Parm notificationIid: InternalId,
  @Parm verificationContent: String
) extends Logging {

  implicit def jdbcConn = injector.instance[JdbcConn]

  def service: JValue = {
    val av = injector.instance[AgentView]
    implicit val jdbcConn = injector.instance[JdbcConn]

    val notification = av.fetch[Notification](notificationIid)
    val connection = av.fetch[Connection](notification.fromConnectionIid)
    val verificationRequest = VerificationRequest.fromJson(notification.data)

    VerifyService.verify(
      av,
      distributedMgr,
      notification.fromConnectionIid,
      verificationRequest.contentIid,
      verificationRequest.contentData,
      verificationContent,
      connection.aliasIid
    )

    av.update[Notification](notification.copy(consumed = true))

    JString("success")
  }
}
