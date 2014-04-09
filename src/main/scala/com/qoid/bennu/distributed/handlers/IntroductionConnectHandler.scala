package com.qoid.bennu.distributed.handlers

import com.qoid.bennu.distributed.messages.IntroductionConnect
import com.qoid.bennu.model._
import com.qoid.bennu.security.AgentView
import com.qoid.bennu.security.AliasSecurityContext
import com.qoid.bennu.security.SecurityContext
import m3.Txn
import m3.jdbc._
import m3.predef._

object IntroductionConnectHandler extends Logging {
  def handle(connection: Connection, introductionConnect: IntroductionConnect, injector: ScalaInjector): Unit = {
    Txn {
      Txn.setViaTypename[SecurityContext](AliasSecurityContext(injector, connection.aliasIid))
      val av = injector.instance[AgentView]

      val notifications = av.select[Notification](sql"json_str(data, 'introductionIid') = ${introductionConnect.introductionIid}").toList

      notifications match {
        case n :: Nil =>
          notification.IntroductionRequest.fromJson(n.data).accepted match {
            case Some(true) =>
              av.insert(Connection(
                aliasIid = connection.aliasIid,
                localPeerId = introductionConnect.localPeerId,
                remotePeerId = introductionConnect.remotePeerId
              ))
            case _ => logger.warn(s"introduction request not accepted for introduction iid -- ${introductionConnect.introductionIid}")
          }
        case _ => logger.warn(s"introduction request notification not found for introduction iid -- ${introductionConnect.introductionIid}")
      }
    }
  }
}
