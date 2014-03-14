package com.qoid.bennu.webservices

import com.google.inject.Inject
import com.qoid.bennu.security.SecurityContext
import com.qoid.bennu.distributed.DistributedManager
import com.qoid.bennu.model._
import com.qoid.bennu.squery._
import java.sql.{ Connection => JdbcConn }
import m3.predef._
import m3.servlet.beans.Parm
import net.liftweb.json._
import scala.language.existentials
import com.qoid.bennu.distributed.messages.{DistributedMessageKind, DistributedMessage, IntroductionRequest}
import net.codingwell.scalaguice.InjectorExtensions.ScalaInjector

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
    //TODO: Use security context

    val aConnection = Connection.fetch(aConnectionIid)
    val bConnection = Connection.fetch(bConnectionIid)

    val introduction = Introduction(aConnectionIid, IntroductionState.NotResponded, bConnectionIid, IntroductionState.NotResponded, securityContext.agentId)
    introduction.sqlInsert.notifyStandingQueries(StandingQueryAction.Insert)

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
