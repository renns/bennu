package com.qoid.bennu.testclient

import com.qoid.bennu.JsonAssist.jsondsl._
import com.qoid.bennu.model.Profile
import com.qoid.bennu.testclient.client.HttpAssist.HttpClientConfig
import com.qoid.bennu.testclient.client._
import m3.guice.GuiceApp
import scala.concurrent.Await
import scala.concurrent.Promise
import scala.concurrent.duration.Duration

object ProfilesIntegrator extends GuiceApp {
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
      ("Profiles - Historical", profilesHistorical),
      ("Profiles - Standing", profilesStanding)
    ).map { t =>
      logger.debug(s"Test started -- ${t._1}")
      val result = t._2()
      logger.debug(s"Test ended -- ${t._1} -- ${if (result.isEmpty) "PASS" else "FAIL"}")
      (t._1, result)
    }
  }

  def profilesHistorical()(implicit config: HttpClientConfig): Option[Exception] = {
    try {
      val p = Promise[Unit]()

      val client1 = HttpAssist.createAgent("Agent1")
      val client2 = HttpAssist.createAgent("Agent2")
      val alias1 = client1.getRootAlias()
      val label2 = client2.getRootLabel()
      val alias2 = client2.createAlias(label2.iid, "Test")
      val (conn1, _) = TestAssist.createConnection(client1, alias1, client2, alias2)

      val expectedResults = List(("name" -> "Test") ~ ("imgSrc" -> ""))
      client1.query[Profile]("", local = false, connections = List(conn1))(TestAssist.handleQueryResponse(_, expectedResults, p))

      Await.result(p.future, Duration("10 seconds"))

      None
    } catch {
      case e: Exception => Some(e)
    }
  }

  def profilesStanding()(implicit config: HttpClientConfig): Option[Exception] = {
    try {
      val p = Promise[Unit]()

      val client1 = HttpAssist.createAgent("Agent1")
      val client2 = HttpAssist.createAgent("Agent2")
      val alias1 = client1.getRootAlias()
      val label2 = client2.getRootLabel()
      val alias2 = client2.createAlias(label2.iid, "Test")
      val (conn1, _) = TestAssist.createConnection(client1, alias1, client2, alias2)

      val expectedResults = List(("name" -> "Test2") ~ ("imgSrc" -> ""))
      client1.query[Profile]("", local = false, connections = List(conn1), historical = false, standing = true)(TestAssist.handleQueryResponse(_, expectedResults, p))

      client2.upsert(alias2.copy(profile = ("name" -> "Test2") ~ ("imgSrc" -> "")))

      Await.result(p.future, Duration("10 seconds"))

      None
    } catch {
      case e: Exception => Some(e)
    }
  }
}
