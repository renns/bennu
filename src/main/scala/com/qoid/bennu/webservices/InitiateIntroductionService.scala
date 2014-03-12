package com.qoid.bennu.webservices

import com.google.inject.Inject
import com.qoid.bennu.SecurityContext.AgentCapableSecurityContext
import com.qoid.bennu.distributed.DistributedManager
import com.qoid.bennu.distributed.DistributedRequestKind
import com.qoid.bennu.model._
import com.qoid.bennu.model.notification.IntroductionRequest
import com.qoid.bennu.squery._
import java.sql.{ Connection => JdbcConn }
import m3.predef._
import m3.servlet.beans.Parm
import net.liftweb.json._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.language.existentials

case class InitiateIntroductionService @Inject()(
  implicit conn: JdbcConn,
  securityContext: AgentCapableSecurityContext,
  sQueryMgr: StandingQueryManager,
  distributedMgr: DistributedManager,
  @Parm aConnectionIid: InternalId,
  @Parm aMessage: String,
  @Parm bConnectionIid: InternalId,
  @Parm bMessage: String
) extends Logging {

  def service: JValue = {
    val aConnection = Connection.fetch(aConnectionIid)
    val bConnection = Connection.fetch(bConnectionIid)

    val introduction = Introduction(aConnectionIid, IntroductionState.NotResponded, bConnectionIid, IntroductionState.NotResponded, securityContext.agentId)
    introduction.sqlInsert.notifyStandingQueries(StandingQueryAction.Insert)

//    for {
//      profileA <- distributedMgr.sendRequest(aConnection.iid, DistributedRequestKind.GetProfile, JNothing)
//      profileB <- distributedMgr.sendRequest(bConnection.iid, DistributedRequestKind.GetProfile, JNothing)
//    } {
//      val aIntroductionRequest = IntroductionRequest(introduction.iid, aMessage, profileB)
//      val bIntroductionRequest = IntroductionRequest(introduction.iid, bMessage, profileA)
//
//      distributedMgr.sendNotification(aConnection.iid, NotificationKind.IntroductionRequest, aIntroductionRequest.toJson)
//      distributedMgr.sendNotification(bConnection.iid, NotificationKind.IntroductionRequest, bIntroductionRequest.toJson)
//    }

    JString("success")
  }
}
