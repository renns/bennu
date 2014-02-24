package com.qoid.bennu.testclient

import com.qoid.bennu.model._
import com.qoid.bennu.testclient.client._
import m3.guice.GuiceApp

object UpsertIntegrator extends GuiceApp {
  implicit val config = HttpAssist.HttpClientConfig()

  insertLabel()
  updateLabel()
  System.exit(0)

  def insertLabel(): Unit = {
    try {
      val client = HttpAssist.createAgent(AgentId("Agent1"))
      val label = Label(client.agentId, "Insert Label")
      client.upsert(label)

      logger.debug("insertLabel: PASS")
    } catch {
      case e: Exception => logger.warn("insertLabel: FAIL -- " + e)
    }
  }

  def updateLabel(): Unit = {
    try {
      val client = HttpAssist.createAgent(AgentId("Agent1"))
      val insertLabel = Label(client.agentId, "Insert Label")
      val newLabel = client.upsert(insertLabel)
      val updateLabel = newLabel.copy(name = "UpdateLabel")
      client.upsert(updateLabel)

      logger.debug("updateLabel: PASS")
    } catch {
      case e: Exception => logger.warn("updateLabel: FAIL -- " + e)
    }
  }
}
