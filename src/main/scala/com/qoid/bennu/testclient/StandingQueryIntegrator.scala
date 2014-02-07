package com.qoid.bennu.testclient

import com.qoid.bennu.model._
import com.qoid.bennu.testclient.client._
import m3.guice.GuiceApp
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent._
import scala.concurrent.duration.Duration

object StandingQueryIntegrator extends GuiceApp {
  implicit val config = HttpAssist.HttpClientConfig()

  insertParentChildLabels()
  System.exit(0)

  def insertParentChildLabels(): Unit = {
    try {
      val p = Promise[Unit]()

      val agentId = AgentId("Agent1")
      ServiceAssist.createAgent(agentId, true)
      val client = ChannelClientFactory.createHttpChannelClient(agentId)

      val fStandingQuery = client.registerStandingQuery(List("label", "labelchild")) {
        case (action, _, label: Label) => logger.debug(s"$action squery event -- $label")
        case (action, handle, labelChild: LabelChild) =>
          logger.debug(s"$action squery event -- $labelChild")

          client.deRegisterStandingQuery(handle).onSuccess { case _ =>
            p.success()
          }
        case _ => logger.debug("Invalid type in standing query")
      }

      Await.result(fStandingQuery, Duration("10 seconds"))

      val fParentLabel = client.createLabel("parent")
      val fChildLabel = client.createLabel("child")

      val parentLabel = Await.result(fParentLabel, Duration("10 seconds"))
      val childLabel = Await.result(fChildLabel, Duration("10 seconds"))

      client.createLabelChild(parentLabel.iid, childLabel.iid)

      Await.result(p.future, Duration("10 seconds"))

      logger.debug("insertParentChildLabels: PASS")
    } catch {
      case e: Exception => logger.warn("insertParentChildLabels: FAIL -- " + e)
    }
  }
}
