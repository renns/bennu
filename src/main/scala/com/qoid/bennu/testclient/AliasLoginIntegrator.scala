package com.qoid.bennu.testclient

import com.qoid.bennu.JsonAssist._
import com.qoid.bennu.testclient.client.HttpAssist.HttpClientConfig
import com.qoid.bennu.testclient.client._
import m3.guice.GuiceApp

object AliasLoginIntegrator extends GuiceApp {
  val results = run()

  println("\nResults:")

  results.foreach {
    case (name, None) => println(s"  $name -- PASS")
    case (name, Some(e)) => println(s"  $name -- FAIL\n${e.getMessage}\n${e.getStackTraceString}")
  }

  System.exit(0)

  def run(): List[(String, Option[Exception])] = {
    implicit val config = HttpAssist.HttpClientConfig()

    List[(String, () => Option[Exception])](
      ("Alias Login - Get Profile", getProfile)
    ).map { t =>
      logger.debug(s"Test started -- ${t._1}")
      val result = t._2()
      logger.debug(s"Test ended -- ${t._1} -- ${if (result.isEmpty) "PASS" else "FAIL"}")
      (t._1, result)
    }
  }

  def getProfile()(implicit config: HttpClientConfig): Option[Exception] = {
    try {
      HttpAssist.createAgent("Agent1")
      val client = ChannelClientFactory.createHttpChannelClient("agent1.anonymous")
      val alias = client.getRootAlias()

      alias.profile \ "name" match {
        case JString("Anonymous") => None
        case n => Some(new Exception(s"Profile name invalid -- $n"))
      }
    } catch {
      case e: Exception => Some(e)
    }
  }
}
