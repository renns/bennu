package com.qoid.bennu.testclient

import com.qoid.bennu.model._
import m3.guice.GuiceApp
import m3.json.LiftJsonAssist.JNothing
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.collection.immutable.HashMap
import net.liftweb.json.JString

object UpsertIntegrator extends GuiceApp {
  insertLabel()
  System.exit(0)

  def insertLabel(): Unit = {
    val agentId = AgentId("007")
    val client = ChannelClientFactory.createHttpChannelClient(agentId)
    val label = Label(InternalId.random, agentId, "Test Label", JNothing)
    val parms = HashMap("type" -> JString("Label"), "instance" -> label.toJson)
    val f = client.post(ApiPath.upsert, parms)
    val response = Await.result(f, Duration("30 seconds"))
    println("insertLabel response: " + response)
  }
}
