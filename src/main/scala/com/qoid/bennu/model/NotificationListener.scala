package com.qoid.bennu.model

import com.google.inject.Singleton
import com.qoid.bennu.model.notification.IntroductionResponse
import com.qoid.bennu.squery.StandingQueryAction
import java.sql.{Connection => JdbcConn}
import m3.jdbc._
import m3.predef._

@Singleton
class NotificationListener extends Logging {
  case class Listener(kind: NotificationKind, fn: Notification => Unit)

  private lazy val _listeners: List[Listener] = List(
    Listener(NotificationKind.IntroductionResponse, listenForIntroductionResponse)
  )
  
  private lazy val _listenersByKind = _listeners.groupBy(_.kind)
  
  def fireNotification(n: Notification) = {
    _listenersByKind.get(n.kind).getOrElse(Nil).foreach(_.fn(n))
  }

  def listenForIntroductionResponse(n: Notification): Unit = {
    try {
      implicit val jdbcConn = inject[JdbcConn]

      val introResponse = IntroductionResponse.fromJson(n.data)

      // TODO: We need to prevent a race condition if A and B respond at the same time
      val intro = Introduction.fetch(introResponse.introductionIid)

      val updatedIntro = n.fromConnectionIid match {
        case intro.aConnectionIid =>
          intro
            .copy(aState = calculateState(introResponse.accepted))
            .sqlUpdate
            .notifyStandingQueries(StandingQueryAction.Update)
        case intro.bConnectionIid =>
          intro
            .copy(bState = calculateState(introResponse.accepted))
            .sqlUpdate
            .notifyStandingQueries(StandingQueryAction.Update)
        case _ => m3x.error("IntroductionResponse notification: fromConnectionIid doesn't match introduction")
      }

      if (updatedIntro.aState == IntroductionState.Accepted && updatedIntro.bState == IntroductionState.Accepted) {
        // TODO: Connections should not be created here. Instead the details should be sent to A and B and they should create their connection
        createConnections(updatedIntro)
      }

      n.copy(consumed = true).sqlUpdate.notifyStandingQueries(StandingQueryAction.Update)
    } catch {
      case e: Exception => logger.warn(s"listenForIntroductionResuponse: FAIL -- $e")
    }

    def calculateState(accepted: Boolean): IntroductionState = {
      if (accepted) IntroductionState.Accepted else IntroductionState.Rejected
    }

    def createConnections(intro: Introduction)(implicit jdbcConn: JdbcConn): Unit = {
      val peerId1 = PeerId.random
      val peerId2 = PeerId.random

      val connToA = Connection.fetch(intro.aConnectionIid)
      val connToB = Connection.fetch(intro.bConnectionIid)
      val connFromA = Connection.selectBox(sql"localPeerId = ${connToA.remotePeerId}").open_$
      val connFromB = Connection.selectBox(sql"localPeerId = ${connToB.remotePeerId}").open_$

      Connection(connFromA.agentId, connFromA.aliasIid, peerId1, peerId2)
        .sqlInsert
        .notifyStandingQueries(StandingQueryAction.Insert)

      Connection(connFromB.agentId, connFromB.aliasIid, peerId2, peerId1)
        .sqlInsert
        .notifyStandingQueries(StandingQueryAction.Insert)
    }
  }
}
