package com.qoid.bennu.testclient

import com.qoid.bennu.JsonAssist.jsondsl._
import com.qoid.bennu.model._
import com.qoid.bennu.testclient.client.HttpAssist.HttpClientConfig
import com.qoid.bennu.testclient.client._
import m3.guice.GuiceApp
import scala.concurrent._
import scala.concurrent.duration.Duration

object QueryIntegrator extends GuiceApp {
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
      ("Query - Local Historical", queryLocalHistorical),
      ("Query - Local Standing", queryLocalStanding),
      ("Query - Sub-Alias Local Historical", querySubAliasLocalHistorical),
      ("Query - Sub-Alias Local Standing", querySubAliasLocalStanding),
      ("Query - Remote Historical", queryRemoteHistorical),
      ("Query - Remote Standing", queryRemoteStanding)
      //TODO: ("Query - Remote Meta-Label Historical", queryRemoteMetaLabelHistorical) -- This isn't working yet
    ).map { t =>
      logger.debug(s"Test started -- ${t._1}")
      val result = t._2()
      logger.debug(s"Test ended -- ${t._1} -- ${if (result.isEmpty) "PASS" else "FAIL"}")
      (t._1, result)
    }
  }

  private def queryLocalHistorical()(implicit config: HttpClientConfig): Option[Exception] = {
    try {
      val p = Promise[Unit]()

      val client = HttpAssist.createAgent("Agent1")
      val alias = client.getRootAlias()
      val label = client.createChildLabel(alias.rootLabelIid, "A")

      val expectedResults = List(label.toJson)
      client.query[Label](s"name = 'A'")(TestAssist.handleQueryResponse(_, expectedResults, p))

      Await.result(p.future, Duration("10 seconds"))

      None
    } catch {
      case e: Exception => Some(e)
    }
  }

  private def queryLocalStanding()(implicit config: HttpClientConfig): Option[Exception] = {
    try {
      val p = Promise[Unit]()

      val client = HttpAssist.createAgent("Agent1")
      val rootLabel = client.getRootLabel()
      val label = Label("A")

      val expectedResults = List(label.toJson)
      client.query[Label](s"name = 'A'", historical = false, standing = true)(TestAssist.handleQueryResponse(_, expectedResults, p))

      client.upsert(label)
      client.upsert(LabelChild(rootLabel.iid, label.iid))

      Await.result(p.future, Duration("10 seconds"))

      None
    } catch {
      case e: Exception => Some(e)
    }
  }

  private def querySubAliasLocalHistorical()(implicit config: HttpClientConfig): Option[Exception] = {
    try {
      val p = Promise[Unit]()

      val client = HttpAssist.createAgent("Agent1")
      val rootLabel = client.getRootLabel()
      val subLabel = client.createChildLabel(rootLabel.iid, "Sub-Alias")
      val subAlias = client.createAlias(subLabel.iid, "Sub-Alias")
      val label = client.createChildLabel(subLabel.iid, "A")

      val expectedResults = List(label.toJson)
      client.query[Label](s"name = 'A'", Some(subAlias))(TestAssist.handleQueryResponse(_, expectedResults, p))

      Await.result(p.future, Duration("10 seconds"))

      None
    } catch {
      case e: Exception => Some(e)
    }
  }

  private def querySubAliasLocalStanding()(implicit config: HttpClientConfig): Option[Exception] = {
    try {
      val p = Promise[Unit]()

      val client = HttpAssist.createAgent("Agent1")
      val rootLabel = client.getRootLabel()
      val subLabel = client.createChildLabel(rootLabel.iid, "Sub-Alias")
      val subAlias = client.createAlias(subLabel.iid, "Sub-Alias")
      val label = Label("A")

      val expectedResults = List(label.toJson)
      client.query[Label](s"name = 'A'", Some(subAlias), historical = false, standing = true)(TestAssist.handleQueryResponse(_, expectedResults, p))

      client.upsert(label)
      client.upsert(LabelChild(subLabel.iid, label.iid))

      Await.result(p.future, Duration("10 seconds"))

      None
    } catch {
      case e: Exception => Some(e)
    }
  }

  private def queryRemoteHistorical()(implicit config: HttpClientConfig): Option[Exception] = {
    try {
      val p = Promise[Unit]()

      val client1 = HttpAssist.createAgent("Agent1")
      val client2 = HttpAssist.createAgent("Agent2")
      val alias1 = client1.getRootAlias()
      val alias2 = client2.getRootAlias()
      val (conn1, conn2) = TestAssist.createConnection(client1, alias1, client2, alias2)
      val (contents, _) = TestAssist.createSampleContent(client2, alias2, Some(conn2))
      val contentC = contents(2)

      val expectedResults = List(contentC.toJson)
      client1.query[Content](s"hasLabelPath('uber label','A','B','C')", local = false, connections = List(conn1))(TestAssist.handleQueryResponse(_, expectedResults, p))

      Await.result(p.future, Duration("10 seconds"))

      None
    } catch {
      case e: Exception => Some(e)
    }
  }

  private def queryRemoteStanding()(implicit config: HttpClientConfig): Option[Exception] = {
    try {
      val p = Promise[Unit]()

      val client1 = HttpAssist.createAgent("Agent1")
      val client2 = HttpAssist.createAgent("Agent2")
      val alias1 = client1.getRootAlias()
      val alias2 = client2.getRootAlias()
      val (conn1, conn2) = TestAssist.createConnection(client1, alias1, client2, alias2)
      val label2 = client2.createChildLabel(alias2.rootLabelIid, "A")
      client2.upsert(LabelAcl(conn2.iid, label2.iid))
      val content = Content(alias2.iid, "text", data = ("text" ->  "Content") ~ ("booyaka" -> "wop"))

      val expectedResults = List(content.toJson)
      client1.query[Content](s"hasLabelPath('uber label','A')", local = false, connections = List(conn1), historical = false, standing = true)(TestAssist.handleQueryResponse(_, expectedResults, p))

      client2.upsert(content)
      client2.upsert(LabeledContent(content.iid, label2.iid))

      Await.result(p.future, Duration("10 seconds"))

      None
    } catch {
      case e: Exception => Some(e)
    }
  }

  private def queryConnectionMetaLabelHistorical()(implicit config: HttpClientConfig): Option[Exception] = {
    try {
      val p = Promise[Unit]()

      val client1 = HttpAssist.createAgent("Agent1")
      val client2 = HttpAssist.createAgent("Agent2")
      val alias1 = client1.getRootAlias()
      val alias2 = client2.getRootAlias()
      val (conn1, conn2) = TestAssist.createConnection(client1, alias1, client2, alias2)
      val content = client2.upsert(Content(alias2.iid, "TEXT", data = "text" ->  "agent 2 should see this"))
      client2.upsert(LabeledContent(content.iid, conn2.metaLabelIid))

      val expectedResults = List(content.toJson)
      //TODO: implement hasConnectionMetaLabel - it would only exist on ConnectionSecurityContext
      client1.query[Content]("hasConnectionMetaLabel()", local = false, connections = List(conn1))(TestAssist.handleQueryResponse(_, expectedResults, p))

      Await.result(p.future, Duration("10 seconds"))

      None
    } catch {
      case e: Exception => Some(e)
    }
  }
}
