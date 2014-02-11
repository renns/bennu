package com.qoid.bennu.testclient

import com.qoid.bennu.model._
import com.qoid.bennu.squery.StandingQueryAction
import com.qoid.bennu.testclient.client._
import m3.guice.GuiceApp
import m3.json.LiftJsonAssist.jsondsl._
import scala.concurrent._
import scala.concurrent.duration.Duration

object PingPongNotificationsIntegrator extends GuiceApp {
  implicit val config = HttpAssist.HttpClientConfig()

  run()
  System.exit(0)

  def run(): Unit = {
    try {
      val p = Promise[Unit]()

      val (client1, _, alias1) = HttpAssist.initAgent(AgentId("Agent1"))
      val (client2, _, alias2) = HttpAssist.initAgent(AgentId("Agent2"))

      val (conn1, _) = TestAssist.createConnection(client1, alias1, client2, alias2)
      client1.registerStandingQuery(List("notification"))(handleStandingQueryResult(_, _, _, client1, p))
      client1.sendNotification(conn1.remotePeerId, "ping", "hello" -> "world")

      Await.result(p.future, Duration("30 seconds"))

      logger.debug("PingPongNotificationsIntegrator: PASS")
    } catch {
      case e: Exception => logger.warn("PingPongNotificationsIntegrator: FAIL -- " + e)
    }

    def handleStandingQueryResult(
      action: StandingQueryAction,
      handle: InternalId,
      instance: HasInternalId,
      client: ChannelClient,
      p: Promise[Unit]
    ): Unit = {
      (action, instance) match {
        case (StandingQueryAction.Insert, n: Notification) =>
          logger.debug(s"notification received $n")
          client.deRegisterStandingQuery(handle)
          p.success()
        case (a, i) => logger.debug(s"Invalid action / type in standing query -- $a / $i")
      }
    }
  }
}
