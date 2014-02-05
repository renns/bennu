package com.qoid.bennu.testclient

import com.qoid.bennu.model._
import m3.guice.GuiceApp
import net.liftweb.json.JNothing
import net.liftweb.json.JString
import scala.collection.immutable.HashMap
import scala.concurrent.Await
import scala.concurrent.duration.Duration

object DeleteIntegrator extends GuiceApp {
  deleteLabel()
  System.exit(0)

  def deleteLabel(): Unit = {
    logger.debug("Starting deleteLabel...")

    val agentId = AgentId("007")
    val client = ChannelClientFactory.createHttpChannelClient(agentId)
    val labelId = InternalId.random

    val insertLabel = Label(labelId, agentId, "Insert Label", JNothing)
    val insertParms = HashMap("type" -> JString("Label"), "instance" -> insertLabel.toJson)
    val insertFuture = client.post(ApiPath.upsert, insertParms)
    val insertResponse = Await.result(insertFuture, Duration("10 seconds"))

    if (insertResponse.success) {
      logger.debug("Insert Result: " + insertResponse.result.toString)

      val deleteParms = HashMap("type" -> JString("Label"), "primaryKey" -> JString(labelId.value))
      val deleteFuture = client.post(ApiPath.delete, deleteParms)
      val deleteResponse = Await.result(deleteFuture, Duration("10 seconds"))

      if (deleteResponse.success) {
        logger.debug("Delete Result: " + deleteResponse.result.toString)
        logger.debug("Finished deleteLabel: Success")
      } else {
        logger.debug("Finished deleteLabel: Delete Failed")
      }
    } else {
      logger.debug("Finished deleteLabel: Insert Failed")
    }
  }
}
