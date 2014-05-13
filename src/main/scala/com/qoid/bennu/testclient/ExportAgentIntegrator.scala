package com.qoid.bennu.testclient

import com.qoid.bennu.JsonAssist._
import com.qoid.bennu.JsonAssist.jsondsl._
import com.qoid.bennu.model._
import com.qoid.bennu.model.id.InternalId
import com.qoid.bennu.model.introduction.IntroductionState
import com.qoid.bennu.model.notification.NotificationKind
import com.qoid.bennu.testclient.client.HttpAssist.HttpClientConfig
import com.qoid.bennu.testclient.client._
import m3.guice.GuiceApp

object ExportAgentIntegrator extends GuiceApp {
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
      ("Export Agent - Export then Import", exportThenImport)
    ).map { t =>
      logger.debug(s"Test started -- ${t._1}")
      val result = t._2()
      logger.debug(s"Test ended -- ${t._1} -- ${if (result.isEmpty) "PASS" else "FAIL"}")
      (t._1, result)
    }
  }

  def exportThenImport()(implicit config: HttpClientConfig): Option[Exception] = {
    try {
      val client = HttpAssist.createAgent("Agent1")
      val alias = client.getRootAlias()
      client.createContent(alias.iid, "TEXT", "text" -> "Content", List(alias.rootLabelIid))
      client.upsert(Introduction(InternalId.random, IntroductionState.NotResponded, InternalId.random, IntroductionState.NotResponded))
      client.upsert(LabelAcl(InternalId.random, alias.rootLabelIid))
      client.upsert(Notification(InternalId.random, NotificationKind.IntroductionRequest))

      val agentData = client.deleteAgent(true)

      if (canLogin("Agent1")) throw new Exception("Able to login after deleting agent")

      HttpAssist.importAgent(agentData)

      val client2 = ChannelClientFactory.createHttpChannelClient("Agent1")
      val agentData2 = client2.deleteAgent(true)

      if (agentData == agentData2) {
        None
      } else {
        throw new Exception(s"Response results not as expected\nReceived:\n${agentData2.toJsonStr}\nExpected:\n${agentData.toJsonStr}")
      }
    } catch {
      case e: Exception => Some(e)
    }
  }

  private def canLogin(authenticationId: String)(implicit config: HttpClientConfig): Boolean = {
    try {
      ChannelClientFactory.createHttpChannelClient(authenticationId)
      true
    } catch {
      case _: Exception => false
    }
  }
}
