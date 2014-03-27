package com.qoid.bennu.testclient

import com.qoid.bennu.JsonAssist._
import com.qoid.bennu.JsonAssist.jsondsl._
import com.qoid.bennu.model._
import com.qoid.bennu.testclient.client.HttpAssist.HttpClientConfig
import com.qoid.bennu.testclient.client._
import m3.guice.GuiceApp
import scala.concurrent._
import scala.concurrent.duration.Duration

object DeRegisterIntegrator extends GuiceApp {
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
      ("De-Register Standing Query - Local", deRegisterLocal),
      ("De-Register Standing Query - Remote", deRegisterRemote)
    ).map { t =>
      logger.debug(s"Test started -- ${t._1}")
      val result = t._2()
      logger.debug(s"Test ended -- ${t._1} -- ${if (result.isEmpty) "PASS" else "FAIL"}")
      (t._1, result)
    }
  }

  def deRegisterLocal()(implicit config: HttpClientConfig): Option[Exception] = {
    def handleQueryResponse(
      response: QueryResponse,
      client: ChannelClient,
      p: Promise[Unit]
    ): Unit = {
      response.results match {
        case JArray(i :: Nil) =>
          val label = Label.fromJson(i)

          if (label.data == JNothing) {
            client.deRegisterStandingQuery(response.handle)
            client.upsert(label.copy(data = JString("test")))
          } else {
            p.success()
          }
        case _ =>
      }
    }

    try {
      val p = Promise[Unit]()

      val client = HttpAssist.createAgent("Agent1")
      val rootLabel = client.getRootLabel()
      val label = Label("A")

      client.query[Label](s"name = 'A'", historical = false, standing = true)(handleQueryResponse(_, client, p))

      client.upsert(label, Some(rootLabel.iid))

      Await.result(p.future, Duration("10 seconds"))

      Some(new Exception("Got result back after de-registering standing query"))
    } catch {
      case e: TimeoutException => None
      case e: Exception => Some(e)
    }
  }

  def deRegisterRemote()(implicit config: HttpClientConfig): Option[Exception] = {
    def handleQueryResponse(
      response: QueryResponse,
      client1: ChannelClient,
      client2: ChannelClient,
      p: Promise[Unit]
    ): Unit = {
      response.results match {
        case JArray(i :: Nil) =>
          val content = Content.fromJson(i)

          if (content.contentType == "TEXT") {
            client1.deRegisterStandingQuery(response.handle)
            client2.upsert(content.copy(contentType = "image"))
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
      val alias1 = client1.getRootAlias()
      val alias2 = client2.getRootAlias()
      val (conn1, conn2) = TestAssist.createConnection(client1, alias1, client2, alias2)
      val label2 = client2.createLabel(alias2.rootLabelIid, "A")
      client2.upsert(LabelAcl(conn2.iid, label2.iid))

      client1.query[Content](s"hasLabelPath('A')", local = false, connections = List(conn1), historical = false, standing = true)(handleQueryResponse(_, client1, client2, p))

      client2.createContent(alias2.iid, "TEXT", ("text" -> "Content") ~ ("booyaka" -> "wop"), Some(List(label2.iid)))

      Await.result(p.future, Duration("10 seconds"))

      Some(new Exception("Got result back after de-registering standing query"))
    } catch {
      case e: TimeoutException => None
      case e: Exception => Some(e)
    }
  }
}
