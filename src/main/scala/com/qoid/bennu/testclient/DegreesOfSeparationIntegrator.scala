package com.qoid.bennu.testclient

import com.qoid.bennu.JsonAssist._
import com.qoid.bennu.JsonAssist.jsondsl._
import com.qoid.bennu.model._
import com.qoid.bennu.testclient.client.HttpAssist.HttpClientConfig
import com.qoid.bennu.testclient.client._
import m3.guice.GuiceApp
import scala.concurrent._
import scala.concurrent.duration.Duration

object DegreesOfSeparationIntegrator extends GuiceApp {
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
      ("Degrees of Separation - 2 Degrees Historical", degrees2Historical),
      ("Degrees of Separation - 2 Degrees Standing", degrees2Standing),
      ("Degrees of Separation - 3 Degrees Historical", degrees3Historical),
      ("Degrees of Separation - 3 Degrees Standing", degrees3Standing),
      ("Degrees of Separation - 2 Degrees De-Register Standing", degrees2DeRegisterStanding)
    ).map { t =>
      logger.debug(s"Test started -- ${t._1}")
      val result = t._2()
      logger.debug(s"Test ended -- ${t._1} -- ${if (result.isEmpty) "PASS" else "FAIL"}")
      (t._1, result)
    }
  }

  def degrees2Historical()(implicit config: HttpClientConfig): Option[Exception] = {
    try {
      val p = Promise[Unit]()

      val client1 = HttpAssist.createAgent("Agent1")
      val client2 = HttpAssist.createAgent("Agent2")
      val client3 = HttpAssist.createAgent("Agent3")
      val alias1 = client1.getRootAlias()
      val alias2 = client2.getRootAlias()
      val alias3 = client3.getRootAlias()
      val (conn12, _) = TestAssist.createConnection(client1, alias1.iid, client2, alias2.iid)
      val (conn23, conn32) = TestAssist.createConnection(client2, alias2.iid, client3, alias3.iid)
      val (contents, _) = TestAssist.createSampleContent(client3, alias3, Some(conn32))
      val contentC = contents(2)

      val expectedResults = List(contentC.toJson)
      client1.query[Content](s"hasLabelPath('A','B','C')", local = false, connectionIids = List(List(conn12.iid, conn23.iid)))(TestAssist.handleQueryResponse(_, expectedResults, p))

      Await.result(p.future, Duration("10 seconds"))

      None
    } catch {
      case e: Exception => Some(e)
    }
  }

  def degrees2Standing()(implicit config: HttpClientConfig): Option[Exception] = {
    try {
      val p = Promise[Unit]()

      val client1 = HttpAssist.createAgent("Agent1")
      val client2 = HttpAssist.createAgent("Agent2")
      val client3 = HttpAssist.createAgent("Agent3")
      val alias1 = client1.getRootAlias()
      val alias2 = client2.getRootAlias()
      val alias3 = client3.getRootAlias()
      val (conn12, _) = TestAssist.createConnection(client1, alias1.iid, client2, alias2.iid)
      val (conn23, conn32) = TestAssist.createConnection(client2, alias2.iid, client3, alias3.iid)
      val label3 = client3.createLabel(alias3.rootLabelIid, "A")
      client3.upsert(LabelAcl(conn32.iid, label3.iid))
      val content = Content(alias3.iid, "text", data = ("text" ->  "Content") ~ ("booyaka" -> "wop"))

      val expectedResults = List(content.toJson)
      client1.query[Content](s"hasLabelPath('A')", local = false, connectionIids = List(List(conn12.iid, conn23.iid)), historical = false, standing = true)(TestAssist.handleQueryResponse(_, expectedResults, p))

      client3.upsert(content, labelIids = List(label3.iid))

      Await.result(p.future, Duration("10 seconds"))

      None
    } catch {
      case e: Exception => Some(e)
    }
  }

  def degrees3Historical()(implicit config: HttpClientConfig): Option[Exception] = {
    try {
      val p = Promise[Unit]()

      val client1 = HttpAssist.createAgent("Agent1")
      val client2 = HttpAssist.createAgent("Agent2")
      val client3 = HttpAssist.createAgent("Agent3")
      val client4 = HttpAssist.createAgent("Agent4")
      val alias1 = client1.getRootAlias()
      val alias2 = client2.getRootAlias()
      val alias3 = client3.getRootAlias()
      val alias4 = client4.getRootAlias()
      val (conn12, _) = TestAssist.createConnection(client1, alias1.iid, client2, alias2.iid)
      val (conn23, _) = TestAssist.createConnection(client2, alias2.iid, client3, alias3.iid)
      val (conn34, conn43) = TestAssist.createConnection(client3, alias3.iid, client4, alias4.iid)
      val (contents, _) = TestAssist.createSampleContent(client4, alias4, Some(conn43))
      val contentC = contents(2)

      val expectedResults = List(contentC.toJson)
      client1.query[Content](s"hasLabelPath('A','B','C')", local = false, connectionIids = List(List(conn12.iid, conn23.iid, conn34.iid)))(TestAssist.handleQueryResponse(_, expectedResults, p))

      Await.result(p.future, Duration("10 seconds"))

      None
    } catch {
      case e: Exception => Some(e)
    }
  }

  def degrees3Standing()(implicit config: HttpClientConfig): Option[Exception] = {
    try {
      val p = Promise[Unit]()

      val client1 = HttpAssist.createAgent("Agent1")
      val client2 = HttpAssist.createAgent("Agent2")
      val client3 = HttpAssist.createAgent("Agent3")
      val client4 = HttpAssist.createAgent("Agent4")
      val alias1 = client1.getRootAlias()
      val alias2 = client2.getRootAlias()
      val alias3 = client3.getRootAlias()
      val alias4 = client4.getRootAlias()
      val (conn12, _) = TestAssist.createConnection(client1, alias1.iid, client2, alias2.iid)
      val (conn23, _) = TestAssist.createConnection(client2, alias2.iid, client3, alias3.iid)
      val (conn34, conn43) = TestAssist.createConnection(client3, alias3.iid, client4, alias4.iid)
      val label4 = client4.createLabel(alias4.rootLabelIid, "A")
      client4.upsert(LabelAcl(conn43.iid, label4.iid))
      val content = Content(alias4.iid, "text", data = ("text" ->  "Content") ~ ("booyaka" -> "wop"))

      val expectedResults = List(content.toJson)
      client1.query[Content](s"hasLabelPath('A')", local = false, connectionIids = List(List(conn12.iid, conn23.iid, conn34.iid)), historical = false, standing = true)(TestAssist.handleQueryResponse(_, expectedResults, p))

      client4.upsert(content, labelIids = List(label4.iid))

      Await.result(p.future, Duration("10 seconds"))

      None
    } catch {
      case e: Exception => Some(e)
    }
  }

  def degrees2DeRegisterStanding()(implicit config: HttpClientConfig): Option[Exception] = {
    def handleQueryResponse(
      response: QueryResponse,
      client1: ChannelClient,
      client3: ChannelClient,
      p: Promise[Unit]
    ): Unit = {
      response.results match {
        case JArray(i :: Nil) =>
          val content = Content.fromJson(i)

          if (content.contentType == "TEXT") {
            client1.deRegisterStandingQuery(response.handle)
            Thread.sleep(3000)
            client3.upsert(content.copy(contentType = "IMAGE"))
          } else {
            p.success()
          }
        case _ =>
      }
    }

    try {
      val p = Promise[Unit]()

      val client1 = HttpAssist.createAgent("Agent1")
      val client2 = HttpAssist.createAgent("Agent2")
      val client3 = HttpAssist.createAgent("Agent3")
      val alias1 = client1.getRootAlias()
      val alias2 = client2.getRootAlias()
      val alias3 = client3.getRootAlias()
      val (conn12, _) = TestAssist.createConnection(client1, alias1.iid, client2, alias2.iid)
      val (conn23, conn32) = TestAssist.createConnection(client2, alias2.iid, client3, alias3.iid)
      val label3 = client3.createLabel(alias3.rootLabelIid, "A")
      client3.upsert(LabelAcl(conn32.iid, label3.iid))

      client1.query[Content](s"hasLabelPath('A')", local = false, connectionIids = List(List(conn12.iid, conn23.iid)), historical = false, standing = true)(handleQueryResponse(_, client1, client3, p))

      client3.createContent(alias3.iid, "TEXT", ("text" -> "Content") ~ ("booyaka" -> "wop"), List(label3.iid))

      Await.result(p.future, Duration("30 seconds"))

      Some(new Exception("Got result back after de-registering standing query"))
    } catch {
      case e: TimeoutException => None
      case e: Exception => Some(e)
    }
  }
}
