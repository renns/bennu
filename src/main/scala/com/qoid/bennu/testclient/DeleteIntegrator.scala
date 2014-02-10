package com.qoid.bennu.testclient

import com.qoid.bennu.model._
import com.qoid.bennu.testclient.client._
import m3.guice.GuiceApp
import m3.json.LiftJsonAssist._

object DeleteIntegrator extends GuiceApp {
  implicit val config = HttpAssist.HttpClientConfig()

  deleteLabel()
  deleteLabelWithWrongAgent()
  System.exit(0)

  def deleteLabel(): Unit = {
    try {
      val (client, _, _) = HttpAssist.initAgent(AgentId("Agent1"))
      val label = Label(InternalId.random, client.agentId, "Insert Label", JNothing)
      val newLabel = client.upsert(label)
      val deletedLabel = client.delete(newLabel)

      if (deletedLabel.deleted) {
        logger.debug("deleteLabel: PASS")
      } else {
        logger.warn("deleteLabel: FAIL -- Returned label not marked deleted")
      }
    } catch {
      case e: Exception => logger.warn("deleteLabel: FAIL -- " + e)
    }
  }

  def deleteLabelWithWrongAgent(): Unit = {
    try {
      val (client1, _, _) = HttpAssist.initAgent(AgentId("Agent1"))
      val (client2, _, _) = HttpAssist.initAgent(AgentId("Agent2"))
      val label = Label(InternalId.random, client1.agentId, "Insert Label", JNothing)
      val newLabel = client1.upsert(label)
      client2.delete(newLabel)

      logger.debug("deleteLabelWithWrongAgent: FAIL -- Validation didn't work")
    } catch {
      case e: Exception => logger.debug("deleteLabelWithWrongAgent: PASS")
    }
  }
}
