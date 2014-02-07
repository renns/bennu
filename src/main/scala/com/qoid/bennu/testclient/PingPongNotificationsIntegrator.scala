package com.qoid.bennu.testclient

import com.qoid.bennu.model._
import com.qoid.bennu.squery.StandingQueryAction
import com.qoid.bennu.testclient.client._
import m3.guice.GuiceApp
import m3.json.LiftJsonAssist.jsondsl._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent._
import scala.concurrent.duration.Duration

object PingPongNotificationsIntegrator extends GuiceApp {
  implicit val config = HttpAssist.HttpClientConfig()

  run()
  System.exit(0)

  def run(): Unit = {
    try {
      val p = Promise[Unit]()

      val agentId1 = AgentId("Agent1")
      val agentId2 = AgentId("Agent2")
      ServiceAssist.createAgent(agentId1, true)
      ServiceAssist.createAgent(agentId2, true)
      val client1 = ChannelClientFactory.createHttpChannelClient(agentId1)
      val client2 = ChannelClientFactory.createHttpChannelClient(agentId2)

      client1.registerStandingQuery(List("notification")) {
        case (StandingQueryAction.Insert, _, n: Notification) =>
          logger.debug(s"notification received ${n}")
          p.success()
        case i => logger.debug(s"Invalid type in standing query -- ${i}")
      }

      for {
        label1 <- client1.createLabel("root")
        label2 <- client2.createLabel("root")
        alias1 <- client1.createAlias(label1.iid, "alias")
        alias2 <- client2.createAlias(label2.iid, "alias")
        conn1 <- client1.createConnection(alias1.iid, PeerId.random, PeerId.random)
        conn2 <- client2.createConnection(alias2.iid, conn1.remotePeerId, conn1.localPeerId)
      } {
        client1.sendNotification(conn1.remotePeerId, "ping", "hello" -> "world")
      }

      Await.result(p.future, Duration("30 seconds"))

      logger.debug("PingPongNotificationsIntegrator: PASS")
    } catch {
      case e: Exception => logger.warn("PingPongNotificationsIntegrator: FAIL -- " + e)
    }
  }
}
