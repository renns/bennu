package com.qoid.bennu.testclient

import com.qoid.bennu.model._
import com.qoid.bennu.testclient.client.HttpAssist.HttpClientConfig
import com.qoid.bennu.testclient.client._
import m3.guice.GuiceApp

object UpsertIntegrator extends GuiceApp {
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
      ("Upsert - Insert Label", insertLabel),
      ("Upsert - Update Label", updateLabel)
    ).map { t =>
      logger.debug(s"Test started -- ${t._1}")
      val result = t._2()
      logger.debug(s"Test ended -- ${t._1} -- ${if (result.isEmpty) "PASS" else "FAIL"}")
      (t._1, result)
    }
  }

  def insertLabel()(implicit config: HttpClientConfig): Option[Exception] = {
    try {
      val client = HttpAssist.createAgent("Agent1")
      val label = Label("Insert Label")
      val returnedLabel = client.upsert(label)

      if (label == returnedLabel) {
        None
      } else {
        Some(new Exception("Returned label doesn't match"))
      }
    } catch {
      case e: Exception => Some(e)
    }
  }

  def updateLabel()(implicit config: HttpClientConfig): Option[Exception] = {
    try {
      val client = HttpAssist.createAgent("Agent1")
      val insertLabel = Label("Insert Label")
      val newLabel = client.upsert(insertLabel)
      val updateLabel = newLabel.copy(name = "UpdateLabel")
      val returnedLabel = client.upsert(updateLabel)

      if (updateLabel == returnedLabel) {
        None
      } else {
        Some(new Exception("Returned label doesn't match"))
      }
    } catch {
      case e: Exception => Some(e)
    }
  }
}
