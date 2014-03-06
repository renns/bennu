package com.qoid.bennu.testclient

import com.qoid.bennu.JsonAssist._
import com.qoid.bennu.testclient.client._
import m3.guice.GuiceApp

object AliasLoginIntegrator extends GuiceApp {
  implicit val config = HttpAssist.HttpClientConfig()

  getProfile()
  System.exit(0)

  def getProfile(): Unit = {
    try {
      HttpAssist.createAgent("Agent1")
      val client = ChannelClientFactory.createHttpChannelClient("agent1.anonymous")
      val alias = client.getRootAlias()

      alias.profile \ "name" match {
        case JString("Anonymous") => logger.debug("getProfile: PASS")
        case n => logger.warn(s"getProfile: FAIL -- Profile name invalid -- $n")
      }
    } catch {
      case e: Exception => logger.warn("getProfile: FAIL", e)
    }
  }
}
