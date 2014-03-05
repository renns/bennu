package com.qoid.bennu.testclient

import com.qoid.bennu.JsonAssist.jsondsl._
import com.qoid.bennu.model._
import com.qoid.bennu.squery.StandingQueryAction
import com.qoid.bennu.testclient.client._
import m3.guice.GuiceApp
import scala.concurrent._
import scala.concurrent.duration.Duration

object StandingQueryIntegrator extends GuiceApp {
  implicit val config = HttpAssist.HttpClientConfig()

  insertParentChildLabels()
  updateProfile()
  System.exit(0)

  def insertParentChildLabels(): Unit = {
    try {
      val p = Promise[Unit]()

      val client = HttpAssist.createAgent("Agent1")
      client.registerStandingQuery(List("label", "labelchild"))(handleStandingQueryResult(_, _, _, client, p))
      val rootLabel = client.getRootLabel()
      val childLabel = client.createLabel("child")
      client.createLabelChild(rootLabel.iid, childLabel.iid)

      Await.result(p.future, Duration("30 seconds"))

      logger.debug("insertParentChildLabels: PASS")
    } catch {
      case e: Exception => logger.warn("insertParentChildLabels: FAIL", e)
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

  def updateProfile(): Unit = {
    try {
      val client1 = HttpAssist.createAgent("Agent1")
      val client2 = HttpAssist.createAgent("Agent2")
      val alias1 = client1.getRootAlias()
      val alias2 = client2.getRootAlias()
      TestAssist.createConnection(client1, alias1, client2, alias2)

      client2.registerStandingQuery(List("profile")){
        // this will never get called, because the squery profile hack breaks standard convention
        // look in the server logs to verify it is working
        case _ =>
      }

      client1.upsert(alias1.copy(profile = ("name" -> "New Uber Alias") ~ ("imgSrc" -> "")))

      logger.debug("updateProfile: PASS")
    } catch {
      case e: Exception => logger.warn("updateProfile: FAIL", e)
    }
  }
}
