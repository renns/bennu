package com.qoid.bennu.testclient

import com.qoid.bennu.JsonAssist.jsondsl._
import com.qoid.bennu.model._
import com.qoid.bennu.testclient.client._
import com.qoid.bennu.webservices.QueryService
import m3.guice.GuiceApp
import scala.concurrent._
import scala.concurrent.duration.Duration
import com.qoid.bennu.squery.StandingQueryAction

object QueryIntegrator extends GuiceApp {
  implicit val config = HttpAssist.HttpClientConfig()

  val results = run()

  println("\nResults:")

  results.foreach {
    case (name, None) => println(s"  $name -- PASS")
    case (name, Some(e)) => println(s"  $name -- FAIL\n${e.getMessage}\n${e.getStackTraceString}")
  }

  System.exit(0)

  def run(): List[(String, Option[Exception])] = {
    List[(String, () => Option[Exception])](
      ("Query - Self Historical", querySelfHistorical),
      ("Query - Self Standing", querySelfStanding),
      ("Query - Sub-Alias Historical", querySubAliasHistorical),
      ("Query - Sub-Alias Standing", querySubAliasStanding),
      ("Query - Connection Historical", queryConnectionHistorical),
      ("Query - Connection Standing", queryConnectionStanding)
      //Connection Historical Profiles
      //Connection Standing Profiles
    ).map { t =>
      logger.debug(s"Test started -- ${t._1}")
      val result = t._2()
      logger.debug(s"Test ended -- ${t._1} -- ${if (result.isEmpty) "PASS" else "FAIL"}")
      (t._1, result)
    }
  }

  def querySelfHistorical(): Option[Exception] = {
    try {
      val p = Promise[Unit]()

      val client = HttpAssist.createAgent("Agent1")
      val alias = client.getRootAlias()
      val label = client.createChildLabel(alias.rootLabelIid, "A")

      val expected = QueryService.ResponseData(Some(alias.iid), None, "label", None, List(label.toJson)).toJson
      client.query[Label](s"name = 'A'")(TestAssist.handleAsyncResponse(_, expected, p))

      Await.result(p.future, Duration("10 seconds"))

      None
    } catch {
      case e: Exception => Some(e)
    }
  }

  def querySelfStanding(): Option[Exception] = {
    try {
      val p = Promise[Unit]()

      val client = HttpAssist.createAgent("Agent1")
      val alias = client.getRootAlias()
      val rootLabel = client.getRootLabel()
      val label = Label("A")

      val expected = QueryService.ResponseData(Some(alias.iid), None, "label", Some(StandingQueryAction.Update), List(label.toJson)).toJson
      client.query[Label](s"name = 'A'", historical = false, standing = true)(TestAssist.handleAsyncResponse(_, expected, p))

      client.upsert(label)
      client.upsert(LabelChild(rootLabel.iid, label.iid))

      Await.result(p.future, Duration("10 seconds"))

      None
    } catch {
      case e: Exception => Some(e)
    }
  }

  def querySubAliasHistorical(): Option[Exception] = {
    try {
      val p = Promise[Unit]()

      val client = HttpAssist.createAgent("Agent1")
      val rootLabel = client.getRootLabel()
      val subLabel = client.createChildLabel(rootLabel.iid, "Sub-Alias")
      val subAlias = client.createAlias(subLabel.iid, "Sub-Alias")
      val label = client.createChildLabel(subLabel.iid, "A")

      val expected = QueryService.ResponseData(Some(subAlias.iid), None, "label", None, List(label.toJson)).toJson
      client.query[Label](s"name = 'A'", Some(subAlias))(TestAssist.handleAsyncResponse(_, expected, p))

      Await.result(p.future, Duration("10 seconds"))

      None
    } catch {
      case e: Exception => Some(e)
    }
  }

  def querySubAliasStanding(): Option[Exception] = {
    try {
      val p = Promise[Unit]()

      val client = HttpAssist.createAgent("Agent1")
      val rootLabel = client.getRootLabel()
      val subLabel = client.createChildLabel(rootLabel.iid, "Sub-Alias")
      val subAlias = client.createAlias(subLabel.iid, "Sub-Alias")
      val label = Label("A")

      val expected = QueryService.ResponseData(Some(subAlias.iid), None, "label", Some(StandingQueryAction.Update), List(label.toJson)).toJson
      client.query[Label](s"name = 'A'", Some(subAlias), historical = false, standing = true)(TestAssist.handleAsyncResponse(_, expected, p))

      client.upsert(label)
      client.upsert(LabelChild(subLabel.iid, label.iid))

      Await.result(p.future, Duration("10 seconds"))

      None
    } catch {
      case e: Exception => Some(e)
    }
  }

  def queryConnectionHistorical(): Option[Exception] = {
    try {
      val p = Promise[Unit]()

      val client1 = HttpAssist.createAgent("Agent1")
      val client2 = HttpAssist.createAgent("Agent2")
      val alias1 = client1.getRootAlias()
      val alias2 = client2.getRootAlias()
      val (conn1, conn2) = TestAssist.createConnection(client1, alias1, client2, alias2)
      val (contents, _) = TestAssist.createSampleContent(client2, alias2, Some(conn2))
      val contentC = contents(2)

      val expected = QueryService.ResponseData(None, Some(conn1.iid), "content", None, Some(List(contentC.toJson))).toJson
      client1.query[Content](s"hasLabelPath('uber label','A','B','C')", None, List(conn1))(TestAssist.handleAsyncResponse(_, expected, p))

      Await.result(p.future, Duration("10 seconds"))

      None
    } catch {
      case e: Exception => Some(e)
    }
  }

  def queryConnectionStanding(): Option[Exception] = {
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

      val expected = QueryService.ResponseData(None, Some(conn1.iid), "content", Some(StandingQueryAction.Update), Some(List(content.toJson))).toJson
      client1.query[Content](s"hasLabelPath('uber label','A')", None, List(conn1), historical = false, standing = true)(TestAssist.handleAsyncResponse(_, expected, p))

      client2.upsert(content)
      client2.upsert(LabeledContent(content.iid, label2.iid))

      Await.result(p.future, Duration("10 seconds"))

      None
    } catch {
      case e: Exception => Some(e)
    }
  }
}
