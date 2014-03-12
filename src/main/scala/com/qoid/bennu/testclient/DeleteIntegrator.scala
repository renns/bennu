package com.qoid.bennu.testclient

import com.qoid.bennu.model._
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
      ("Delete - Delete Label", deleteLabel),
      ("Delete - Delete Label With Wrong Agent", deleteLabelWithWrongAgent)
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
      val label = Label("Insert Label")
      val newLabel = client.upsert(label)
      val deletedLabel = client.delete(newLabel)

      if (deletedLabel.deleted) {
        None
      } else {
        Some(new Exception("Returned label not marked deleted"))
      }
    } catch {
      case e: Exception => Some(e)
    }
  }

  def deleteLabelWithWrongAgent()(implicit config: HttpClientConfig): Option[Exception] = {
    try {
      val client1 = HttpAssist.createAgent("Agent1")
      val client2 = HttpAssist.createAgent("Agent2")
      val label = Label("Insert Label")
      val newLabel = client1.upsert(label)
      client2.delete(newLabel)

      Some(new Exception("Validation didn't work"))
    } catch {
      case e: Exception => None
    }
  }
}
