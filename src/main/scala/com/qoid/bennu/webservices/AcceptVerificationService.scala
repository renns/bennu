package com.qoid.bennu.webservices

import com.google.inject.Inject
import com.qoid.bennu.JsonAssist._
import com.qoid.bennu.model._
import com.qoid.bennu.model.id.InternalId
import com.qoid.bennu.model.notification.VerificationResponse
import com.qoid.bennu.security.AgentView
import m3.predef._
import m3.servlet.beans.Parm

case class AcceptVerificationService @Inject() (
  injector: ScalaInjector,
  @Parm notificationIid: InternalId,
  @Parm verificationContent: String
) extends Logging {

  def service: JValue = {
    val av = injector.instance[AgentView]

    val notification = av.fetch[Notification](notificationIid)
    val verificationResponse = VerificationResponse.fromJson(notification.data)

    val content = av.fetch[Content](verificationResponse.contentIid)
    val metaData = Content.MetaData.fromJson(content.metaData)
    val verification = Content.MetaDataVerification(verificationResponse.verifierId, verificationResponse.verificationContentIid, verificationContent, "COPY")

    val verifications = metaData.verifications match {
      case Some(v) => verification :: v
      case None => List(verification)
    }

    av.update[Content](content.copy(metaData = metaData.copy(verifications = Some(verifications)).toJson))

    av.update[Notification](notification.copy(consumed = true))

    JString("success")
  }
}
