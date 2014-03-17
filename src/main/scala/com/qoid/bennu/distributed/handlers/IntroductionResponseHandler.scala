package com.qoid.bennu.distributed.handlers

import com.qoid.bennu.distributed.DistributedManager
import com.qoid.bennu.distributed.messages._
import com.qoid.bennu.model._
import com.qoid.bennu.model.introduction.IntroductionState
import com.qoid.bennu.security.AgentView
import com.qoid.bennu.security.AliasSecurityContext
import com.qoid.bennu.security.SecurityContext
import m3.Txn
import m3.predef._

object IntroductionResponseHandler extends Logging {
  def handle(connection: Connection, introductionResponse: IntroductionResponse, injector: ScalaInjector): Unit = {
    Txn {
      Txn.setViaTypename[SecurityContext](AliasSecurityContext(injector, connection.aliasIid))
      val av = injector.instance[AgentView]

      // TODO: We need to prevent a race condition if A and B respond at the same time
      val introduction = av.fetch[Introduction](introductionResponse.introductionIid)

      val updatedIntroduction = connection.iid match {
        case introduction.aConnectionIid =>
          av.update(introduction.copy(aState = calculateState(introductionResponse.accepted)))
        case introduction.bConnectionIid =>
          av.update(introduction.copy(bState = calculateState(introductionResponse.accepted)))
        case _ => m3x.error("IntroductionResponseHandler -- Connection iid doesn't match introduction")
      }

      if (
        updatedIntroduction.aState == IntroductionState.Accepted &&
        updatedIntroduction.bState == IntroductionState.Accepted
      ) {
        connect(updatedIntroduction, av, injector.instance[DistributedManager])
      }
    }
  }

  private def calculateState(accepted: Boolean): IntroductionState = {
    if (accepted) IntroductionState.Accepted else IntroductionState.Rejected
  }

  private def connect(introduction: Introduction, av: AgentView, distributedMgr: DistributedManager): Unit = {
    val peerId1 = PeerId.random
    val peerId2 = PeerId.random

    val aConnection = av.fetch[Connection](introduction.aConnectionIid)
    val bConnection = av.fetch[Connection](introduction.bConnectionIid)

    distributedMgr.send(
      aConnection,
      DistributedMessage(
        DistributedMessageKind.IntroductionConnect,
        1,
        IntroductionConnect(introduction.iid, peerId1, peerId2).toJson
      )
    )

    distributedMgr.send(
      bConnection,
      DistributedMessage(
        DistributedMessageKind.IntroductionConnect,
        1,
        IntroductionConnect(introduction.iid, peerId2, peerId1).toJson
      )
    )
  }
}
