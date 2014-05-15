package com.qoid.bennu.testclient

import com.qoid.bennu.model.Label
import com.qoid.bennu.testclient.client.HttpAssist.HttpClientConfig
import com.qoid.bennu.testclient.client._
import m3.guice.GuiceApp

object DeleteIntegrator extends GuiceApp {
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
      ("Delete - Delete Label", deleteLabel)
    ).map { t =>
      logger.debug(s"Test started -- ${t._1}")
      val result = t._2()
      logger.debug(s"Test ended -- ${t._1} -- ${if (result.isEmpty) "PASS" else "FAIL"}")
      (t._1, result)
    }
  }

  def deleteLabel()(implicit config: HttpClientConfig): Option[Exception] = {
    try {
      val client = HttpAssist.createAgent("Agent1")
      val rootLabel = client.getRootLabel()
      val label = client.createLabel(rootLabel.iid, "Label")
      client.delete[Label](label.iid)

      None
    } catch {
      case e: Exception => Some(e)
    }
  }
}
