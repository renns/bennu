package com.qoid.bennu.distributed.handlers

import com.qoid.bennu.distributed.messages.IntroductionResponse
import com.qoid.bennu.model.Connection
import com.qoid.bennu.security.AgentView
import com.qoid.bennu.security.AliasSecurityContext
import com.qoid.bennu.security.SecurityContext
import m3.Txn
import m3.predef._

object IntroductionResponseHandler {
  def handle(connection: Connection, introductionResponse: IntroductionResponse, injector: ScalaInjector): Unit = {
    //TODO: This is a security vulnerability and can be removed when we only allow polling of messages
    //Switch to Alias security context
    Txn.setViaTypename[SecurityContext](AliasSecurityContext(injector, connection.aliasIid))
    val av = injector.instance[AgentView]


  }
}

//      // TODO: We need to prevent a race condition if A and B respond at the same time
//      val intro = Introduction.fetch(introResponse.introductionIid)
//
//      val updatedIntro = n.fromConnectionIid match {
//        case intro.aConnectionIid =>
//          intro
//            .copy(aState = calculateState(introResponse.accepted))
//            .sqlUpdate
//            .notifyStandingQueries(StandingQueryAction.Update)
//        case intro.bConnectionIid =>
//          intro
//            .copy(bState = calculateState(introResponse.accepted))
//            .sqlUpdate
//            .notifyStandingQueries(StandingQueryAction.Update)
//        case _ => m3x.error("IntroductionResponse notification: fromConnectionIid doesn't match introduction")
//      }
//
//      if (updatedIntro.aState == IntroductionState.Accepted && updatedIntro.bState == IntroductionState.Accepted) {
//        // TODO: Connections should not be created here. Instead the details should be sent to A and B and they should create their connection
//        createConnections(updatedIntro)
//      }
//
//    def calculateState(accepted: Boolean): IntroductionState = {
//      if (accepted) IntroductionState.Accepted else IntroductionState.Rejected
//    }
//
//    def createConnections(intro: Introduction)(implicit jdbcConn: JdbcConn): Unit = {
//
//      val peerId1 = PeerId.random
//      val peerId2 = PeerId.random
//
//      val connToA = Connection.fetch(intro.aConnectionIid)
//      val connToB = Connection.fetch(intro.bConnectionIid)
//      val connFromA = Connection.selectBox(sql"localPeerId = ${connToA.remotePeerId}").open_$
//      val connFromB = Connection.selectBox(sql"localPeerId = ${connToB.remotePeerId}").open_$
//
//      val connLabelA = Label(
//        agentId = connToA.agentId,
//        name = "connection"
//      )
//      val connLabelB = Label(
//        agentId = connToB.agentId,
//        name = "connection"
//      )
//
//      Connection(connFromA.aliasIid, connLabelA.iid, peerId1, peerId2, connFromA.agentId)
//        .sqlInsert
//        .notifyStandingQueries(StandingQueryAction.Insert)
//
//      Connection(connFromB.aliasIid, connLabelB.iid, peerId2, peerId1, connFromB.agentId)
//        .sqlInsert
//        .notifyStandingQueries(StandingQueryAction.Insert)
//
//    }