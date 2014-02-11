package com.qoid.bennu.testclient

import com.qoid.bennu.model._
import com.qoid.bennu.testclient.client._
import m3.guice.GuiceApp

object DeleteIntegrator extends GuiceApp {
  implicit val config = HttpAssist.HttpClientConfig()

  deleteLabel()
  deleteLabelWithWrongAgent()
  System.exit(0)

  def deleteLabel(): Unit = {
    try {
      val client = HttpAssist.createAgent(AgentId("Agent1"))
      val label = Label(client.agentId, "Insert Label")
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
      val client1 = HttpAssist.createAgent(AgentId("Agent1"))
      val client2 = HttpAssist.createAgent(AgentId("Agent2"))
      val label = Label(client1.agentId, "Insert Label")
      val newLabel = client1.upsert(label)
      client2.delete(newLabel)

      logger.debug("deleteLabelWithWrongAgent: FAIL -- Validation didn't work")
    } catch {
      case e: Exception => logger.debug("deleteLabelWithWrongAgent: PASS")
    }
  }
}
