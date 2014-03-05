package com.qoid.bennu.testclient

import com.qoid.bennu.model.Label
import com.qoid.bennu.testclient.client.HttpAssist
import m3.guice.GuiceApp

/**
 * Creates two agents then runs a label query to ensure we only see labels from the
 * agent we requested labels for
 */
object TwoAgentsAndAQuery extends GuiceApp {
  implicit val config = HttpAssist.HttpClientConfig()

  run()
  System.exit(0)

  def run(): Unit = {
    try {
      val client1 = HttpAssist.createAgent("Agent1")
      HttpAssist.createAgent("Agent2")
      val results = client1.query[Label]("name = 'uber label'")

      if (results.length == 1) {
        logger.debug("TwoAgentsAndAQuery: PASS")
      } else {
        logger.warn("TwoAgentsAndAQuery: FAIL -- expected 1 result; was " + results.length)
      }
    } catch {
      case e: Exception => logger.warn("TwoAgentsAndAQuery: FAIL", e)
    }
  }
}
