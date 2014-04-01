package com.qoid.bennu.webservices

import com.google.inject.Inject
import com.qoid.bennu.JsonAssist._
import com.qoid.bennu.JsonAssist.jsondsl._
import com.qoid.bennu.distributed.DistributedManager
import com.qoid.bennu.distributed.messages._
import com.qoid.bennu.model._
import com.qoid.bennu.model.id._
import com.qoid.bennu.security._
import java.sql.{ Connection => JdbcConn }
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
  @Parm contentData: JValue,
  @Parm verificationContent: String
) extends Logging {

  def service: JValue = {
    val av = injector.instance[AgentView]
    implicit val jdbcConn = injector.instance[JdbcConn]

    VerifyService.verify(
      av,
      distributedMgr,
      connectionIid,
      contentIid,
      contentData,
      verificationContent,
      securityContext.agentId,
      securityContext.aliasIid
    )

    JString("success")
  }
}

object VerifyService {
  def verify(
    av: AgentView,
    distributedMgr: DistributedManager,
    connectionIid: InternalId,
    contentIid: InternalId,
    contentData: JValue,
    verificationContent: String,
    agentId: AgentId,
    aliasIid: InternalId
  )(
    implicit jdbcConn: JdbcConn
  ): Unit = {

    val now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new DateTime().asDate())

    // Insert verification content
    val verification = av.insert[Content](Content(
      aliasIid,
      "VERIFICATION",
      agentId = agentId,
      data = ("created" -> now) ~ ("modified" -> now) ~ ("text" -> verificationContent),
      metaData = Content.MetaData(Some(Content.MetaDataVerifiedContent(contentData, "COPY"))).toJson
    ))

    // Get Verifications Meta-Label
    val alias = av.fetch[Alias](aliasIid)
    val rootLabel = av.fetch[Label](alias.rootLabelIid)
    val metaLabel = av.findChildLabel(rootLabel.iid, Alias.metaLabelName).head
    val verificationsMetaLabel = av.findChildLabel(metaLabel.iid, Alias.verificationsLabelName).head

    // Label verification content with the verifications meta-label
    av.insert[LabeledContent](LabeledContent(
      verification.iid,
      verificationsMetaLabel.iid,
      agentId = agentId
    ))

    // Get Profile
    val connection = av.fetch[Connection](connectionIid)
    val profile = av.select[Profile](sql"aliasIid = ${connection.aliasIid}").toList.head

    // Send Verification Response message
    distributedMgr.send(
      connection,
      DistributedMessage(
        DistributedMessageKind.VerificationResponse,
        1,
        VerificationResponse(contentIid, verification.iid, verification.data, profile.sharedId).toJson
      )
    )
  }
}
