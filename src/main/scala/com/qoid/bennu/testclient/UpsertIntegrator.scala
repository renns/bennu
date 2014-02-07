package com.qoid.bennu.testclient

import com.qoid.bennu.model._
import com.qoid.bennu.testclient.client._
import m3.guice.GuiceApp
import m3.json.LiftJsonAssist._
import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration

object UpsertIntegrator extends GuiceApp {
  implicit val config = HttpAssist.HttpClientConfig()

  insertLabel()
  updateLabel()
  insertLabelWithWrongAgent()
  updateLabelWithWrongAgent()
  System.exit(0)

  def insertLabel(): Unit = {
    try {
      val agentId = AgentId("Agent1")
      ServiceAssist.createAgent(agentId, true)
      val client = ChannelClientFactory.createHttpChannelClient(agentId)
      val label = Label(InternalId.random, agentId, "Insert Label", JNothing)
      val fInsert = client.upsert(label)

      Await.result(fInsert, Duration("10 seconds"))

      logger.debug("insertLabel: PASS")
    } catch {
      case e: Exception => logger.warn("insertLabel: FAIL -- " + e)
    }
  }

  def updateLabel(): Unit = {
    try {
      val agentId = AgentId("Agent1")
      ServiceAssist.createAgent(agentId, true)
      val client = ChannelClientFactory.createHttpChannelClient(agentId)
      val insertLabel = Label(InternalId.random, agentId, "Insert Label", JNothing)
      val fInsert = client.upsert(insertLabel)

      val newLabel = Await.result(fInsert, Duration("10 seconds"))

      val updateLabel = Label(newLabel.iid, agentId, "Update Label", JNothing)
      val fUpdate = client.upsert(updateLabel)

      Await.result(fUpdate, Duration("10 seconds"))

      logger.debug("updateLabel: PASS")
    } catch {
      case e: Exception => logger.warn("updateLabel: FAIL -- " + e)
    }
  }

  def insertLabelWithWrongAgent(): Unit = {
    try {
      val agentId1 = AgentId("Agent1")
      val agentId2 = AgentId("Agent2")
      ServiceAssist.createAgent(agentId1, true)
      val client = ChannelClientFactory.createHttpChannelClient(agentId1)
      val label = Label(InternalId.random, agentId2, "Insert Label", JNothing)
      val fInsert = client.upsert(label)

      Await.result(fInsert, Duration("10 seconds"))

      logger.warn("insertLabelWithWrongAgent: FAIL -- Validation didn't work")
    } catch {
      case e: Exception => logger.debug("insertLabelWithWrongAgent: PASS")
    }
  }

  def updateLabelWithWrongAgent(): Unit = {
    try {
      val agentId1 = AgentId("Agent1")
      val agentId2 = AgentId("Agent2")
      ServiceAssist.createAgent(agentId1, true)
      val client = ChannelClientFactory.createHttpChannelClient(agentId1)
      val insertLabel = Label(InternalId.random, agentId1, "Insert Label", JNothing)
      val fInsert = client.upsert(insertLabel)

      val newLabel = Await.result(fInsert, Duration("10 seconds"))

      val updateLabel = Label(newLabel.iid, agentId2, "Update Label", JNothing)
      val fUpdate = client.upsert(updateLabel)

      Await.result(fUpdate, Duration("10 seconds"))

      logger.warn("updateLabelWithWrongAgent: FAIL -- Validation didn't work")
    } catch {
      case e: Exception => logger.debug("updateLabelWithWrongAgent: PASS")
    }
  }
}
