package com.qoid.bennu.webservices

import com.google.inject.Inject
import com.qoid.bennu.JsonAssist._
import com.qoid.bennu.JsonAssist.jsondsl._
import com.qoid.bennu.distributed.DistributedManager
import com.qoid.bennu.distributed.messages._
import com.qoid.bennu.model._
import com.qoid.bennu.model.id.InternalId
import com.qoid.bennu.security._
import java.text.SimpleDateFormat
import m3.jdbc._
import m3.predef._
import m3.servlet.beans.Parm
import net.model3.chrono.DateTime

case class VerifyService @Inject() (
  injector: ScalaInjector,
  distributedMgr: DistributedManager,
  securityContext: SecurityContext,
  @Parm connectionIid: InternalId,
  @Parm contentIid: InternalId,
  @Parm content: String,
  @Parm message: String,
  @Parm notificationIid: Option[InternalId] = None
) extends Logging {

  def service: JValue = {
    val av = injector.instance[AgentView]

    val now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new DateTime().asDate())

    val verification = Content(
      securityContext.aliasIid,
      "VERIFICATION",
      agentId = securityContext.agentId,
      data = ("created" -> now) ~ ("modified" -> now) ~ ("text" -> message),
      metaData = Content.MetaData(Some(Content.MetaDataVerifiedContent(content, "COPY"))).toJson
    )

    av.insert[Content](verification)
    // TODO: Put verification content under Verifications label

    // Mark notification as consumed
    notificationIid.foreach { iid =>
      val notification = av.fetch[Notification](iid).copy(consumed = true)
      av.update[Notification](notification)
    }

    val connection = av.fetch[Connection](connectionIid)
    val profile = av.select[Profile](sql"aliasIid = ${connection.aliasIid}").toList.head

    distributedMgr.send(
      connection,
      DistributedMessage(
        DistributedMessageKind.VerificationResponse,
        1,
        VerificationResponse(contentIid, verification.iid, profile.sharedId).toJson
      )
    )

    JString("success")
  }
}
