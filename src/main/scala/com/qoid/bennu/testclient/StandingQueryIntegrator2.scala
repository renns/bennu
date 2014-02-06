package com.qoid.bennu.testclient

import com.qoid.bennu.model._
import m3.guice.GuiceApp
import net.liftweb.json._
import scala.collection.immutable.HashMap
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent._
import scala.concurrent.duration.Duration

object StandingQueryIntegrator2 extends GuiceApp {
  insertParentChildLabels()
  System.exit(0)

  def insertParentChildLabels(): Unit = {
    logger.debug("Starting insertParentChildLabels...")

    // This promise is completed when the whole test should be finished
    val p = Promise[Unit]()

    val agentId = AgentId("007")
    val client = ChannelClientFactory.createHttpChannelClient(agentId)

    val squeryFuture = client.registerStandingQuery(List("label", "labelchild")) {
      case (_, label: Label) => logger.debug("Label: " + label)
      case (handle, labelChild: LabelChild) =>
        logger.debug("LabelChild: " + labelChild)

        // After standing query is de-registered, complete this test
        client.deRegisterStandingQuery(handle).onSuccess { case _ =>
          p.success()
        }
      case _ => logger.debug("Invalid type in standing query")
    }

    // Wait until the standing query is registered and then create labels
    squeryFuture.onSuccess { case handle =>
      val parentLabelIid = InternalId.random
      val childLabelIid = InternalId.random
      val labelChildIid = InternalId.random

      val parentLabel = Label(parentLabelIid, agentId, "parent", JNothing)
      val parentLabelParms = HashMap("type" -> JString("label"), "instance" -> parentLabel.toJson)
      val parentLabelFuture = client.post(ApiPath.upsert, parentLabelParms)

      val childLabel = Label(childLabelIid, agentId, "child", JNothing)
      val childLabelParms = HashMap("type" -> JString("label"), "instance" -> childLabel.toJson)
      val childLabelFuture = client.post(ApiPath.upsert, childLabelParms)

      // Insert parent and child labels in parallel
      // After labels are inserted, create parent-child label mapping
      for {
        parentLabelResponse <- parentLabelFuture
        childLabelResponse <- childLabelFuture
        if parentLabelResponse.success && childLabelResponse.success
      } {
        val labelChild = LabelChild(labelChildIid, agentId, parentLabelIid, childLabelIid, JNothing)
        val labelChildParms = HashMap("type" -> JString("labelchild"), "instance" -> labelChild.toJson)
        client.post(ApiPath.upsert, labelChildParms)
      }
    }

    try {
      Await.ready(p.future, Duration("20 seconds"))
      logger.debug("Finished insertParentChildLabels: Success")
    } catch {
      case e: Exception => logger.debug("Finished insertParentChildLabels: Failure")
    }
  }
}
