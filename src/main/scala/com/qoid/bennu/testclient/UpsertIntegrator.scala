package com.qoid.bennu.testclient

import com.qoid.bennu.model._
import m3.guice.GuiceApp
import net.liftweb.json.JNothing
import net.liftweb.json.JString
import scala.collection.immutable.HashMap
import scala.concurrent.Await
import scala.concurrent.duration.Duration

object UpsertIntegrator extends GuiceApp {
  insertLabel()
  updateLabel()
  insertLabelWithWrongAgent()
  updateLabelWithWrongAgent()
  System.exit(0)

  def insertLabel(): Unit = {
    logger.debug("Starting insertLabel...")

    val agentId = AgentId("007")
    val client = ChannelClientFactory.createHttpChannelClient(agentId)
    val label = Label(InternalId.random, agentId, "Insert Label", JNothing)
    val parms = HashMap("type" -> JString("Label"), "instance" -> label.toJson)
    val f = client.post(ApiPath.upsert, parms)
    val response = Await.result(f, Duration("10 seconds"))

    if (response.success) {
      logger.debug("Insert Result: " + response.result.toString)
      logger.debug("Finished insertLabel: Success")
    } else {
      logger.debug("Finished insertLabel: Failed")
    }
  }

  def updateLabel(): Unit = {
    logger.debug("Starting updateLabel...")

    val agentId = AgentId("007")
    val client = ChannelClientFactory.createHttpChannelClient(agentId)
    val labelId = InternalId.random

    val insertLabel = Label(labelId, agentId, "Insert Label", JNothing)
    val insertParms = HashMap("type" -> JString("Label"), "instance" -> insertLabel.toJson)
    val insertFuture = client.post(ApiPath.upsert, insertParms)
    val insertResponse = Await.result(insertFuture, Duration("10 seconds"))

    if (insertResponse.success) {
      logger.debug("Insert Result: " + insertResponse.result.toString)

      val updateLabel = Label(labelId, agentId, "Update Label", JNothing)
      val updateParms = HashMap("type" -> JString("Label"), "instance" -> updateLabel.toJson)
      val updateFuture = client.post(ApiPath.upsert, updateParms)
      val updateResponse = Await.result(updateFuture, Duration("10 seconds"))

      if (updateResponse.success) {
        logger.debug("Update Result: " + updateResponse.result.toString)
        logger.debug("Finished updateLabel: Success")
      } else {
        logger.debug("Finished updateLabel: Update Failed")
      }
    } else {
      logger.debug("Finished updateLabel: Insert Failed")
    }
  }

  def insertLabelWithWrongAgent(): Unit = {
    logger.debug("Starting insertLabelWithWrongAgent...")

    val agentId = AgentId("007")
    val wrongAgentId = AgentId("008")
    val client = ChannelClientFactory.createHttpChannelClient(agentId)
    val label = Label(InternalId.random, wrongAgentId, "Insert Label", JNothing)
    val parms = HashMap("type" -> JString("Label"), "instance" -> label.toJson)
    val f = client.post(ApiPath.upsert, parms)
    val response = Await.result(f, Duration("10 seconds"))

    if (response.success) {
      logger.debug("Insert Result: " + response.result.toString)
      logger.debug("Finished insertLabelWithWrongAgent: Success")
    } else {
      logger.debug("Finished insertLabelWithWrongAgent: Failed")
    }
  }

  def updateLabelWithWrongAgent(): Unit = {
    logger.debug("Starting updateLabelWithWrongAgent...")

    val agentId = AgentId("007")
    val wrongAgentId = AgentId("008")
    val client = ChannelClientFactory.createHttpChannelClient(agentId)
    val labelId = InternalId.random

    val insertLabel = Label(labelId, agentId, "Insert Label", JNothing)
    val insertParms = HashMap("type" -> JString("Label"), "instance" -> insertLabel.toJson)
    val insertFuture = client.post(ApiPath.upsert, insertParms)
    val insertResponse = Await.result(insertFuture, Duration("10 seconds"))

    if (insertResponse.success) {
      logger.debug("Insert Result: " + insertResponse.result.toString)

      val updateLabel = Label(labelId, wrongAgentId, "Update Label", JNothing)
      val updateParms = HashMap("type" -> JString("Label"), "instance" -> updateLabel.toJson)
      val updateFuture = client.post(ApiPath.upsert, updateParms)
      val updateResponse = Await.result(updateFuture, Duration("10 seconds"))

      if (updateResponse.success) {
        logger.debug("Update Result: " + updateResponse.result.toString)
        logger.debug("Finished updateLabelWithWrongAgent: Success")
      } else {
        logger.debug("Finished updateLabelWithWrongAgent: Update Failed")
      }
    } else {
      logger.debug("Finished updateLabelWithWrongAgent: Insert Failed")
    }
  }
}
