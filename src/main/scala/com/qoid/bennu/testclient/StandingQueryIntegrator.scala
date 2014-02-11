package com.qoid.bennu.testclient

import com.qoid.bennu.model._
import com.qoid.bennu.squery.StandingQueryAction
import com.qoid.bennu.testclient.client._
import m3.guice.GuiceApp
import scala.concurrent._
import scala.concurrent.duration.Duration

object StandingQueryIntegrator extends GuiceApp {
  implicit val config = HttpAssist.HttpClientConfig()

  insertParentChildLabels()
  System.exit(0)

  def insertParentChildLabels(): Unit = {
    try {
      val p = Promise[Unit]()

      val client = HttpAssist.createAgent(AgentId("Agent1"))
      client.registerStandingQuery(List("label", "labelchild"))(handleStandingQueryResult(_, _, _, client, p))
      val parentLabel = client.createLabel("parent")
      val childLabel = client.createLabel("child")
      client.createLabelChild(parentLabel.iid, childLabel.iid)

      Await.result(p.future, Duration("30 seconds"))

      logger.debug("insertParentChildLabels: PASS")
    } catch {
      case e: Exception => logger.warn("insertParentChildLabels: FAIL -- " + e)
    }

    def handleStandingQueryResult(
      action: StandingQueryAction,
      handle: InternalId,
      instance: HasInternalId,
      client: ChannelClient,
      p: Promise[Unit]
    ): Unit = {
      instance match {
        case label: Label => logger.debug(s"$action squery event -- $label")
        case labelChild: LabelChild =>
          logger.debug(s"$action squery event -- $labelChild")
          client.deRegisterStandingQuery(handle)
          p.success()
        case _ => logger.debug("Invalid type in standing query")
      }
    }
  }
}
