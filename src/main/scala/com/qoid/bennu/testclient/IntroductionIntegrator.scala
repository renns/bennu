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
      val clientA = HttpAssist.createAgent("A")
      val clientB = HttpAssist.createAgent("B")
      val clientC = HttpAssist.createAgent("C")
      val aliasA = clientA.getRootAlias()
      val aliasB = clientB.getRootAlias()
      val aliasC = clientC.getRootAlias()
      val connAIntro = clientA.query[Connection]("").head
      val connBIntro = clientB.query[Connection]("").head
      val (connAC, connCA) = TestAssist.createConnection(clientA, aliasA, clientC, aliasC)
      val (connBC, connCB) = TestAssist.createConnection(clientB, aliasB, clientC, aliasC)

      clientC.initiateIntroduction(connCA, "Message to A", connCB, "Message to B")

      // Give C time to send notifications
      Thread.sleep(1000)

      val notificationA = clientA.query[Notification](s"consumed = false and fromConnectionIid = '${connAC.iid.value}' and kind = '${NotificationKind.IntroductionRequest}'").head
      logger.debug(s"Received notification -- $notificationA")
      clientA.respondToIntroduction(notificationA, aAccept)

      val notificationB = clientB.query[Notification](s"consumed = false and fromConnectionIid = '${connBC.iid.value}' and kind = '${NotificationKind.IntroductionRequest}'").head
      logger.debug(s"Received notification -- $notificationB")
      clientB.respondToIntroduction(notificationB, bAccept)

      // Give C time to create connections
      Thread.sleep(1000)

      val aConnections = clientA.query[Connection](sql"iid <> ${connAC.iid} and iid <> ${connAIntro.iid}")
      val bConnections = clientB.query[Connection](sql"iid <> ${connBC.iid} and iid <> ${connBIntro.iid}")

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
      case e: Exception => logger.warn(s"$testName: FAIL", e)
    }
  }

  def runWithStandingQueries(): Unit = {
    try {
      val pA = Promise[Connection]()
      val pB = Promise[Connection]()

      val clientA = HttpAssist.createAgent("A")
      val clientB = HttpAssist.createAgent("B")
      val clientC = HttpAssist.createAgent("C")
      val aliasA = clientA.getRootAlias()
      val aliasB = clientB.getRootAlias()
      val aliasC = clientC.getRootAlias()
      val (_, connCA) = TestAssist.createConnection(clientA, aliasA, clientC, aliasC)
      val (_, connCB) = TestAssist.createConnection(clientB, aliasB, clientC, aliasC)

      clientA.registerStandingQuery(List("notification", "connection"))(handleStandingQueryResult(_, _, _, clientA, pA))
      clientB.registerStandingQuery(List("notification", "connection"))(handleStandingQueryResult(_, _, _, clientB, pB))

      clientC.initiateIntroduction(connCA, "Message to A", connCB, "Message to B")

      val connAB = Await.result(pA.future, Duration("30 seconds"))
      val connBA = Await.result(pB.future, Duration("30 seconds"))

      if (connAB.localPeerId == connBA.remotePeerId && connAB.remotePeerId == connBA.localPeerId) {
        logger.debug("IntroductionWithStandingQueries: PASS")
      } else {
        logger.warn("IntroductionWithStandingQueries: FAIL -- Created connections are not a pair")
      }
    } catch {
      case e: Exception => logger.warn("IntroductionWithStandingQueries: FAIL", e)
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
          logger.debug(s"Received notification -- $n")
          client.respondToIntroduction(n, true)
        case (StandingQueryAction.Insert, c: Connection) =>
          logger.debug(s"Connection created -- $c")
          client.deRegisterStandingQuery(handle)
          p.success(c)
        case _ => ()
      }
    }
  }
}
