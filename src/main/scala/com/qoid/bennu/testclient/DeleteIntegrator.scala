package com.qoid.bennu.testclient

import com.qoid.bennu.model._
import com.qoid.bennu.testclient.client._
import m3.guice.GuiceApp
import m3.json.LiftJsonAssist._
import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration

object DeleteIntegrator extends GuiceApp {
  implicit val config = HttpAssist.HttpClientConfig()

  deleteLabel()
  deleteLabelWithWrongAgent()
  System.exit(0)

  def deleteLabel(): Unit = {
    try {
      val agentId = AgentId("Agent1")
      ServiceAssist.createAgent(agentId, true)
      val client = ChannelClientFactory.createHttpChannelClient(agentId)
      val label = Label(InternalId.random, agentId, "Insert Label", JNothing)
      val fInsert = client.upsert(label)

      val newLabel = Await.result(fInsert, Duration("10 seconds"))

      val fDelete = client.delete(newLabel)

      val deletedLabel = Await.result(fDelete, Duration("10 seconds"))

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
      val agentId1 = AgentId("Agent1")
      val agentId2 = AgentId("Agent2")
      ServiceAssist.createAgent(agentId1, true)
      ServiceAssist.createAgent(agentId2, true)
      val client1 = ChannelClientFactory.createHttpChannelClient(agentId1)
      val client2 = ChannelClientFactory.createHttpChannelClient(agentId2)
      val label = Label(InternalId.random, agentId1, "Insert Label", JNothing)
      val fInsert = client1.upsert(label)

      val newLabel = Await.result(fInsert, Duration("10 seconds"))

      val fDelete = client2.delete(newLabel)

      Await.result(fDelete, Duration("10 seconds"))

      logger.debug("deleteLabelWithWrongAgent: FAIL -- Validation didn't work")
    } catch {
      case e: Exception => logger.debug("deleteLabelWithWrongAgent: PASS")
    }
  }
}
