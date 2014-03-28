package com.qoid.bennu.distributed.handlers

import com.qoid.bennu.distributed.messages._
import com.qoid.bennu.model._
import com.qoid.bennu.model.notification.NotificationKind
import com.qoid.bennu.security._
import m3.Txn
import m3.predef._

object VerificationRequestHandler {
  def handle(connection: Connection, verificationRequest: VerificationRequest, injector: ScalaInjector): Unit = {
    Txn {
      Txn.setViaTypename[SecurityContext](AliasSecurityContext(injector, connection.aliasIid))
      val av = injector.instance[AgentView]

      val data = notification.VerificationRequest(
        verificationRequest.contentIid,
        verificationRequest.contentType,
        verificationRequest.contentData,
        verificationRequest.message
      )

      val n = Notification(
        fromConnectionIid = connection.iid,
        kind = NotificationKind.VerificationRequest,
        agentId = connection.agentId,
        data = data.toJson
      )

      av.insert[Notification](n)
    }
  }
}
