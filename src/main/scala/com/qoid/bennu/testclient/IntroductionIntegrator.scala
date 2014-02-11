package com.qoid.bennu.testclient

import com.qoid.bennu.model._
import com.qoid.bennu.testclient.client._
import m3.guice.GuiceApp
import m3.jdbc._

object IntroductionIntegrator extends GuiceApp {
  implicit val config = HttpAssist.HttpClientConfig()

  run()
  //test with standing queries
  //test where A and B accept
  //test where A accepts and B rejects
  //test where A rejects and B accepts
  //test where A and B reject
  System.exit(0)

  def run(): Unit = {
    try {
      // Initialize agents and connections
      val clientA = HttpAssist.createAgent(AgentId("A"))
      val clientB = HttpAssist.createAgent(AgentId("B"))
      val clientC = HttpAssist.createAgent(AgentId("C"))
      val aliasA = clientA.getUberAlias()
      val aliasB = clientB.getUberAlias()
      val aliasC = clientC.getUberAlias()
      val (connAC, connCA) = TestAssist.createConnection(clientA, aliasA, clientC, aliasC)
      val (connBC, connCB) = TestAssist.createConnection(clientB, aliasB, clientC, aliasC)

      // Initiate the introduction
      clientC.initiateIntroduction(connCA, "Message to A", connCB, "Message to B")

      // Give C time to send notifications
      Thread.sleep(2000)

      // A retrieves notifications and responds
      val notificationA = clientA.query[Notification](s"consumed = false and fromConnectionIid = '${connAC.iid.value}' and kind = '${NotificationKind.IntroductionRequest}'").head
      clientA.respondToIntroduction(notificationA, accepted = true)

      // B retrieves notifications and responds
      val notificationB = clientB.query[Notification](s"consumed = false and fromConnectionIid = '${connBC.iid.value}' and kind = '${NotificationKind.IntroductionRequest}'").head
      clientB.respondToIntroduction(notificationB, accepted = true)

      // Give C time to create connections
      Thread.sleep(2000)

      // Look for new connections
      val aConnection = clientA.query[Connection](sql"iid <> ${connAC.iid}").head
      val bConnection = clientB.query[Connection](sql"iid <> ${connBC.iid}").head

      if (aConnection.localPeerId == bConnection.remotePeerId && aConnection.remotePeerId == bConnection.localPeerId) {
        logger.debug("Introduction: PASS")
      } else {
        logger.warn("Introduction: FAIL -- Created connections are not a pair")
      }
    } catch {
      case e: Exception => logger.warn("Introduction: FAIL -- " + e)
    }
  }
}
