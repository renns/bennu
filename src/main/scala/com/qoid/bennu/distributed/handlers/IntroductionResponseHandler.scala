package com.qoid.bennu.distributed.handlers

import com.qoid.bennu.distributed.DistributedManager
import com.qoid.bennu.distributed.messages._
import com.qoid.bennu.model._
import com.qoid.bennu.model.id.PeerId
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

      val introduction = av.fetch[Introduction](introductionResponse.introductionIid)

      val updatedIntroduction = connection.iid match {
        case introduction.aConnectionIid =>
          updateWithRetry(av, introduction, Some(calculateState(introductionResponse.accepted)), None)
        case introduction.bConnectionIid =>
          updateWithRetry(av, introduction, None, Some(calculateState(introductionResponse.accepted)))
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

  private def updateWithRetry(
    av: AgentView,
    introduction: Introduction,
    aState: Option[IntroductionState],
    bState: Option[IntroductionState]
  ): Introduction = {
    try {
      val i = introduction.copy(
        aState = aState.getOrElse(introduction.aState),
        bState = bState.getOrElse(introduction.bState)
      )
      av.update(i)
    } catch {
      case _: Exception =>
        val i0 = av.fetch[Introduction](introduction.iid)
        val i1 = i0.copy(
          aState = aState.getOrElse(i0.aState),
          bState = bState.getOrElse(i0.bState)
        )
        av.update(i1)
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
