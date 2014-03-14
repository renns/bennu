package com.qoid.bennu.distributed.handlers

import com.qoid.bennu.distributed.messages._
import com.qoid.bennu.model.Connection
import com.qoid.bennu.model.Notification
import com.qoid.bennu.model.NotificationKind
import com.qoid.bennu.security.AgentView
import com.qoid.bennu.security.AliasSecurityContext
import com.qoid.bennu.security.SecurityContext
import m3.Txn
import m3.predef._

object IntroductionRequestHandler {
  def handle(connection: Connection, introductionRequest: IntroductionRequest, injector: ScalaInjector): Unit = {
    //TODO: This is a security vulnerability and can be removed when we only allow polling of messages
    //Switch to Alias security context
    Txn.setViaTypename[SecurityContext](AliasSecurityContext(injector, connection.aliasIid))
    val av = injector.instance[AgentView]

    val notification = Notification(
      fromConnectionIid = connection.iid,
      kind = NotificationKind.IntroductionRequest,
      agentId = connection.agentId,
      data = introductionRequest.toJson
    )

    av.insert[Notification](notification)
  }
}
