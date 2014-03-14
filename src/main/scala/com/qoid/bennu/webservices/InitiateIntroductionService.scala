package com.qoid.bennu.webservices

import com.google.inject.Inject
import com.qoid.bennu.distributed.DistributedManager
import com.qoid.bennu.distributed.messages._
import com.qoid.bennu.model._
import com.qoid.bennu.security.AgentView
import com.qoid.bennu.security.SecurityContext
import java.sql.{ Connection => JdbcConn }
import m3.predef._
import m3.servlet.beans.Parm
import net.codingwell.scalaguice.InjectorExtensions.ScalaInjector
import net.liftweb.json._
import scala.language.existentials

case class InitiateIntroductionService @Inject()(
  injector: ScalaInjector,
  securityContext: SecurityContext,
  distributedMgr: DistributedManager,
  @Parm aConnectionIid: InternalId,
  @Parm aMessage: String,
  @Parm bConnectionIid: InternalId,
  @Parm bMessage: String
) extends Logging {

  implicit def jdbcConn = injector.instance[JdbcConn]

  def service: JValue = {
    val av = injector.instance[AgentView]

    val aConnection = av.fetch[Connection](aConnectionIid)
    val bConnection = av.fetch[Connection](bConnectionIid)

    val introduction = Introduction(aConnectionIid, IntroductionState.NotResponded, bConnectionIid, IntroductionState.NotResponded, securityContext.agentId)
    av.insert(introduction)

    //TODO: Get profiles
    val profileA = JNothing
    val profileB = JNothing

    distributedMgr.send(
      aConnection,
      DistributedMessage(
        DistributedMessageKind.IntroductionRequest,
        1,
        IntroductionRequest(introduction.iid, aMessage, profileB).toJson
      )
    )

    distributedMgr.send(
      bConnection,
      DistributedMessage(
        DistributedMessageKind.IntroductionRequest,
        1,
        IntroductionRequest(introduction.iid, bMessage, profileA).toJson
      )
    )

    JString("success")
  }
}
