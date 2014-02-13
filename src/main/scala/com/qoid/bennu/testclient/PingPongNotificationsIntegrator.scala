package com.qoid.bennu.testclient

import com.qoid.bennu.model._
import com.qoid.bennu.squery.StandingQueryAction
import com.qoid.bennu.testclient.client._
import m3.guice.GuiceApp
import m3.json.LiftJsonAssist._
import scala.concurrent._
import scala.concurrent.duration.Duration

object PingPongNotificationsIntegrator extends GuiceApp {
  implicit val config = HttpAssist.HttpClientConfig()

  run()
  System.exit(0)

  def run(): Unit = {
    try {
      val p = Promise[Unit]()

      val client1 = HttpAssist.createAgent(AgentId("Agent1"))
      val client2 = HttpAssist.createAgent(AgentId("Agent2"))
      val alias1 = client1.getUberAlias()
      val alias2 = client2.getUberAlias()
      val (conn1, _) = TestAssist.createConnection(client1, alias1, client2, alias2)

      client1.registerStandingQuery(List("notification"))(handleStandingQueryResult(_, _, _, client1, p))
      client2.registerStandingQuery(List("notification"))(handleStandingQueryResult(_, _, _, client2, p))
      client1.sendNotification(conn1.iid, NotificationKind.Ping, JString("hello"))

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
          n.kind match {
            case NotificationKind.Ping =>
              client.deRegisterStandingQuery(handle)
              client.sendNotification(n.fromConnectionIid, NotificationKind.Pong, JString("world"))
            case NotificationKind.Pong =>
              client.deRegisterStandingQuery(handle)
              p.success()
            case _ =>
          }
        case _ =>
      }
    }
  }
}
