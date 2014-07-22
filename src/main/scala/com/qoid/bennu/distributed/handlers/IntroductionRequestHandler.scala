//package com.qoid.bennu.distributed.handlers
//
//import com.qoid.bennu.distributed.messages._
//import com.qoid.bennu.model.Connection
//import com.qoid.bennu.model.Notification
//import com.qoid.bennu.model.notification
//import com.qoid.bennu.model.notification.NotificationKind
//import com.qoid.bennu.security.AgentView
//import com.qoid.bennu.security.AliasSecurityContext
//import com.qoid.bennu.security.SecurityContext
//import m3.Txn
//import m3.predef._
//
//object IntroductionRequestHandler {
//  def handle(connection: Connection, introductionRequest: IntroductionRequest, injector: ScalaInjector): Unit = {
//    Txn {
//      Txn.setViaTypename[SecurityContext](AliasSecurityContext(injector, connection.aliasIid))
//      val av = injector.instance[AgentView]
//
//      val data = notification.IntroductionRequest(
//        introductionRequest.introductionIid,
//        introductionRequest.message,
//        introductionRequest.profile
//      )
//
//      val n = Notification(
//        fromConnectionIid = connection.iid,
//        kind = NotificationKind.IntroductionRequest,
//        data = data.toJson
//      )
//
//      av.insert[Notification](n)
//    }
//  }
//}
