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
      ("Query - Remote Standing", queryRemoteStanding),
      ("Query - Remote Meta-Label Historical", queryRemoteMetaLabelHistorical),
      ("Query - Remote Connections", queryRemoteConnectionsHistorical)
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
      val label = client.createLabel(alias.rootLabelIid, "A")

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
      val label = Label("A", createdByAliasIid = client.rootAliasIid, modifiedByAliasIid = client.rootAliasIid)

      val expectedResults = List(label.toJson)
      client.query[Label](s"name = 'A'", historical = false, standing = true)(TestAssist.handleQueryResponse(_, expectedResults, p))

      client.upsert(label, Some(rootLabel.iid))

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
      val subAlias = client.createAlias(rootLabel.iid, "Sub-Alias")
      val label = client.createLabel(subAlias.rootLabelIid, "A")

      val expectedResults = List(label.toJson)
      client.query[Label](s"name = 'A'", Some(subAlias.iid))(TestAssist.handleQueryResponse(_, expectedResults, p))

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
      val subAlias = client.createAlias(rootLabel.iid, "Sub-Alias")
      val label = Label("A", createdByAliasIid = client.rootAliasIid, modifiedByAliasIid = client.rootAliasIid)

      val expectedResults = List(label.toJson)
      client.query[Label](s"name = 'A'", Some(subAlias.iid), historical = false, standing = true)(TestAssist.handleQueryResponse(_, expectedResults, p))

      client.upsert(label, Some(subAlias.rootLabelIid))

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
      val (conn1, conn2) = TestAssist.createConnection(client1, alias1.iid, client2, alias2.iid)
      val (contents, _) = TestAssist.createSampleContent(client2, alias2, Some(conn2))
      val contentC = contents(2)

      val expectedResults = List(contentC.toJson)
      client1.query[Content](s"hasLabelPath('A','B','C')", local = false, connectionIids = List(List(conn1.iid)))(TestAssist.handleQueryResponse(_, expectedResults, p))

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
      val (conn1, conn2) = TestAssist.createConnection(client1, alias1.iid, client2, alias2.iid)
      val label2 = client2.createLabel(alias2.rootLabelIid, "A")
      client2.upsert(LabelAcl(conn2.iid, label2.iid))
      val content = Content(alias2.iid, "text", data = ("text" ->  "Content") ~ ("booyaka" -> "wop"), createdByAliasIid = alias2.iid, modifiedByAliasIid = alias2.iid)

      val expectedResults = List(content.toJson)
      client1.query[Content](s"hasLabelPath('A')", local = false, connectionIids = List(List(conn1.iid)), historical = false, standing = true)(TestAssist.handleQueryResponse(_, expectedResults, p))

      client2.upsert(content, labelIids = List(label2.iid))

      Await.result(p.future, Duration("10 seconds"))

      None
    } catch {
      case e: Exception => Some(e)
    }
  }

  private def queryRemoteMetaLabelHistorical()(implicit config: HttpClientConfig): Option[Exception] = {
    try {
      val p = Promise[Unit]()

      val client1 = HttpAssist.createAgent("Agent1")
      val client2 = HttpAssist.createAgent("Agent2")
      val alias1 = client1.getRootAlias()
      val alias2 = client2.getRootAlias()
      val (conn1, conn2) = TestAssist.createConnection(client1, alias1.iid, client2, alias2.iid)
      val content = client2.createContent(alias2.iid, "TEXT", "text" -> "agent 2 should see this", List(conn2.metaLabelIid))

      val expectedResults = List(content.toJson)
      client1.query[Content]("hasConnectionMetaLabel()", local = false, connectionIids = List(List(conn1.iid)))(TestAssist.handleQueryResponse(_, expectedResults, p))

      Await.result(p.future, Duration("10 seconds"))

      None
    } catch {
      case e: Exception => Some(e)
    }
  }

  private def queryRemoteConnectionsHistorical()(implicit config: HttpClientConfig): Option[Exception] = {
    try {
      val p = Promise[Unit]()

      val client1 = HttpAssist.createAgent("Agent1")
      val client2 = HttpAssist.createAgent("Agent2")
      val alias1 = client1.getRootAlias()
      val alias2 = client2.getRootAlias()
      val (conn12, conn21) = TestAssist.createConnection(client1, alias1.iid, client2, alias2.iid)

      val expectedResults = List(conn21.toJson)
      client1.query[Connection]("", local = false, connectionIids = List(List(conn12.iid)))(TestAssist.handleQueryResponse(_, expectedResults, p))

      Await.result(p.future, Duration("10 seconds"))

      None
    } catch {
      case e: Exception => Some(e)
    }
  }
}
