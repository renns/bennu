package com.qoid.bennu.distributed.handlers

import com.qoid.bennu.distributed.messages._
import com.qoid.bennu.model._
import com.qoid.bennu.model.notification.NotificationKind
import com.qoid.bennu.security._
import m3.Txn
import m3.predef._

object VerificationResponseHandler {
  def handle(connection: Connection, verificationResponse: VerificationResponse, injector: ScalaInjector): Unit = {
    Txn {
      Txn.setViaTypename[SecurityContext](AliasSecurityContext(injector, connection.aliasIid))
      val av = injector.instance[AgentView]

      val data = notification.VerificationResponse(
        verificationResponse.contentIid,
        verificationResponse.verificationContentIid,
        verificationResponse.verificationContentData,
        verificationResponse.verifierId
      )

      val n = Notification(
        fromConnectionIid = connection.iid,
        kind = NotificationKind.VerificationResponse,
        data = data.toJson
      )

      av.insert[Notification](n)
    }
  }
}
