package com.qoid.bennu.testclient

import com.qoid.bennu.model._
import com.qoid.bennu.testclient.client._
import m3.guice.GuiceApp
import m3.json.LiftJsonAssist._

object UpsertIntegrator extends GuiceApp {
  implicit val config = HttpAssist.HttpClientConfig()

  insertLabel()
  updateLabel()
  insertLabelWithWrongAgent()
  updateLabelWithWrongAgent()
  System.exit(0)

  def insertLabel(): Unit = {
    try {
      val (client, _, _) = HttpAssist.initAgent(AgentId("Agent1"))
      val label = Label(InternalId.random, client.agentId, "Insert Label", JNothing)
      client.upsert(label)

      logger.debug("insertLabel: PASS")
    } catch {
      case e: Exception => logger.warn("insertLabel: FAIL -- " + e)
    }
  }

  def updateLabel(): Unit = {
    try {
      val (client, _, _) = HttpAssist.initAgent(AgentId("Agent1"))
      val insertLabel = Label(InternalId.random, client.agentId, "Insert Label", JNothing)
      val newLabel = client.upsert(insertLabel)
      val updateLabel = Label(newLabel.iid, client.agentId, "Update Label", JNothing)
      client.upsert(updateLabel)

      logger.debug("updateLabel: PASS")
    } catch {
      case e: Exception => logger.warn("updateLabel: FAIL -- " + e)
    }
  }

  def insertLabelWithWrongAgent(): Unit = {
    try {
      val (client, _, _) = HttpAssist.initAgent(AgentId("Agent1"))
      val label = Label(InternalId.random, AgentId("Agent2"), "Insert Label", JNothing)
      client.upsert(label)

      logger.warn("insertLabelWithWrongAgent: FAIL -- Validation didn't work")
    } catch {
      case e: Exception => logger.debug("insertLabelWithWrongAgent: PASS")
    }
  }

  def updateLabelWithWrongAgent(): Unit = {
    try {
      val (client, _, _) = HttpAssist.initAgent(AgentId("Agent1"))
      val insertLabel = Label(InternalId.random, client.agentId, "Insert Label", JNothing)
      val newLabel = client.upsert(insertLabel)
      val updateLabel = Label(newLabel.iid, AgentId("Agent2"), "Update Label", JNothing)
      client.upsert(updateLabel)

      logger.warn("updateLabelWithWrongAgent: FAIL -- Validation didn't work")
    } catch {
      case e: Exception => logger.debug("updateLabelWithWrongAgent: PASS")
    }
  }
}
