package com.qoid.bennu.webservices

import com.google.inject.Inject
import com.qoid.bennu.distributed.DistributedManager
import com.qoid.bennu.distributed.messages._
import com.qoid.bennu.model._
import com.qoid.bennu.security.AgentView
import java.sql.{ Connection => JdbcConn }
import m3.predef._
import m3.servlet.beans.Parm
import net.liftweb.json._
import scala.language.existentials

case class RespondToIntroductionService @Inject()(
  injector: ScalaInjector,
  distributedMgr: DistributedManager,
  @Parm notificationIid: InternalId,
  @Parm accepted: Boolean
) extends Logging {

  implicit def jdbcConn = injector.instance[JdbcConn]

  def service: JValue = {
    val av = injector.instance[AgentView]

    val notification = av.fetch[Notification](notificationIid)
    av.update[Notification](notification.copy(consumed = true))

    val connection = av.fetch[Connection](notification.fromConnectionIid)

    val introductionRequest = IntroductionRequest.fromJson(notification.data)

    distributedMgr.send(
      connection,
      DistributedMessage(
        DistributedMessageKind.IntroductionResponse,
        1,
        IntroductionResponse(introductionRequest.introductionIid, accepted).toJson
      )
    )

    JString("success")
  }
}
