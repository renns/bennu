package com.qoid.bennu.testclient

import com.qoid.bennu.model._
import com.qoid.bennu.squery.StandingQueryAction
import com.qoid.bennu.testclient.client._
import m3.guice.GuiceApp
import m3.jdbc._
import scala.concurrent._
import scala.concurrent.duration.Duration

object IntroductionIntegrator extends GuiceApp {
  implicit val config = HttpAssist.HttpClientConfig()

  run(true, true)
  run(true, false)
  run(false, true)
  run(false, false)
  runWithStandingQueries()
  System.exit(0)

  def run(aAccept: Boolean, bAccept: Boolean): Unit = {
    val testName = "Introduction (" + (if (aAccept) "accept" else "reject") + "/" + (if (bAccept) "accept" else "reject") + ")"

    try {
      val clientA = HttpAssist.createAgent(AgentId("A"))
      val clientB = HttpAssist.createAgent(AgentId("B"))
      val clientC = HttpAssist.createAgent(AgentId("C"))
      val aliasA = clientA.getUberAlias()
      val aliasB = clientB.getUberAlias()
      val aliasC = clientC.getUberAlias()
      val (connAC, connCA) = TestAssist.createConnection(clientA, aliasA, clientC, aliasC)
      val (connBC, connCB) = TestAssist.createConnection(clientB, aliasB, clientC, aliasC)

      clientC.initiateIntroduction(connCA, "Message to A", connCB, "Message to B")

      // Give C time to send notifications
      Thread.sleep(1000)

      val notificationA = clientA.query[Notification](s"consumed = false and fromConnectionIid = '${connAC.iid.value}' and kind = '${NotificationKind.IntroductionRequest}'").head
      clientA.respondToIntroduction(notificationA, aAccept)

      val notificationB = clientB.query[Notification](s"consumed = false and fromConnectionIid = '${connBC.iid.value}' and kind = '${NotificationKind.IntroductionRequest}'").head
      clientB.respondToIntroduction(notificationB, bAccept)

      // Give C time to create connections
      Thread.sleep(1000)

      // TODO: Change queries to also ignore connections to introducer
      val aConnections = clientA.query[Connection](sql"iid <> ${connAC.iid}")
      val bConnections = clientB.query[Connection](sql"iid <> ${connBC.iid}")

      if (aAccept && bAccept) {
        if (aConnections.head.localPeerId == bConnections.head.remotePeerId && aConnections.head.remotePeerId == bConnections.head.localPeerId) {
          logger.debug(s"$testName: PASS")
        } else {
          logger.warn(s"$testName: FAIL -- Created connections are not a pair")
        }
      } else {
        if (aConnections.isEmpty && bConnections.isEmpty) {
          logger.debug(s"$testName: PASS")
        } else {
          logger.warn(s"$testName: FAIL -- Connections created when introduction was rejected")
        }
      }
    } catch {
      case e: Exception => logger.warn(s"$testName: FAIL -- $e")
    }
  }

  def runWithStandingQueries(): Unit = {
    try {
      val pA = Promise[Connection]()
      val pB = Promise[Connection]()

      val clientA = HttpAssist.createAgent(AgentId("A"))
      val clientB = HttpAssist.createAgent(AgentId("B"))
      val clientC = HttpAssist.createAgent(AgentId("C"))
      val aliasA = clientA.getUberAlias()
      val aliasB = clientB.getUberAlias()
      val aliasC = clientC.getUberAlias()
      val (_, connCA) = TestAssist.createConnection(clientA, aliasA, clientC, aliasC)
      val (_, connCB) = TestAssist.createConnection(clientB, aliasB, clientC, aliasC)

      clientA.registerStandingQuery(List("notification", "connection"))(handleStandingQueryResult(_, _, _, clientA, pA))
      clientB.registerStandingQuery(List("notification", "connection"))(handleStandingQueryResult(_, _, _, clientB, pB))

      clientC.initiateIntroduction(connCA, "Message to A", connCB, "Message to B")

      val connAB = Await.result(pA.future, Duration("10 seconds"))
      val connBA = Await.result(pB.future, Duration("10 seconds"))

      if (connAB.localPeerId == connBA.remotePeerId && connAB.remotePeerId == connBA.localPeerId) {
        logger.debug("IntroductionWithStandingQueries: PASS")
      } else {
        logger.warn("IntroductionWithStandingQueries: FAIL -- Created connections are not a pair")
      }
    } catch {
      case e: Exception => logger.warn(s"IntroductionWithStandingQueries: FAIL -- $e")
    }

    def handleStandingQueryResult(
      action: StandingQueryAction,
      handle: InternalId,
      instance: HasInternalId,
      client: ChannelClient,
      p: Promise[Connection]
    ): Unit = {
      (action, instance) match {
        case (StandingQueryAction.Insert, n: Notification) if n.kind == NotificationKind.IntroductionRequest =>
          client.respondToIntroduction(n, true)
        case (StandingQueryAction.Insert, c: Connection) =>
          client.deRegisterStandingQuery(handle)
          p.success(c)
        case _ => ()
      }
    }
  }
}
