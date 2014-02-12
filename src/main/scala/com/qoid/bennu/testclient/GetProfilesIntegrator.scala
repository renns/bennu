package com.qoid.bennu.testclient

import com.qoid.bennu.JsonAssist._
import com.qoid.bennu.model._
import com.qoid.bennu.testclient.client._
import m3.guice.GuiceApp

object GetProfilesIntegrator extends GuiceApp {
  implicit val config = HttpAssist.HttpClientConfig()

  run()
  System.exit(0)

  def run(): Unit = {
    try {
      val client = HttpAssist.createAgent(AgentId("Agent1"))
      val connections = client.query[Connection]("")
      val profiles = client.getProfiles(connections)

      println(profiles.toJsonStr)

      logger.debug("GetProfiles: PASS")
    } catch {
      case e: Exception => logger.warn("GetProfiles: FAIL -- " + e)
    }
  }
}
