package com.qoid.bennu.webservices

import com.google.inject.Inject
import com.qoid.bennu.distributed.DistributedManager
import com.qoid.bennu.distributed.messages.DistributedMessage
import com.qoid.bennu.distributed.messages.DistributedMessageKind
import com.qoid.bennu.distributed.messages.IntroductionResponse
import com.qoid.bennu.model._
import com.qoid.bennu.model.id.InternalId
import com.qoid.bennu.model.notification.IntroductionRequest
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
    val introductionRequest = IntroductionRequest.fromJson(notification.data)

    av.update[Notification](notification.copy(
      consumed = true,
      data = introductionRequest.copy(accepted = Some(accepted)).toJson
    ))

    val connection = av.fetch[Connection](notification.fromConnectionIid)

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
