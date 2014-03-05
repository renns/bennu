package com.qoid.bennu.testclient

import com.qoid.bennu.model._
import com.qoid.bennu.testclient.client._
import m3.guice.GuiceApp
import scala.concurrent._
import scala.concurrent.duration.Duration
import com.qoid.bennu.JsonAssist._
import com.qoid.bennu.JsonAssist.jsondsl._
import com.qoid.bennu.webservices.DistributedQueryService

object QueryConnMetaLabelIntegrator extends GuiceApp {
  
  implicit val config = HttpAssist.HttpClientConfig()

  run()
  System.exit(0)

  def run(): Unit = {
    try {
      val p1 = Promise[Unit]()

      val client1 = HttpAssist.createAgent("Agent1")
      val alias1 = client1.getRootAlias()

      val client2 = HttpAssist.createAgent("Agent2")
      val alias2 = client1.getRootAlias()
      
      val (conn1to2, conn2to1) = TestAssist.createConnection(client1, alias1, client2, alias2)
      
      val content1 = client1.upsert(Content(
        aliasIid = alias1.iid,
        contentType = "TEXT",
        data = 
          ("text" ->  "agent 2 should see this") ~
          ("created" -> "2014-03-05 11:03:36") ~
          ("modified" -> "2014-03-05 11:03:36")
      ))

      client1.upsert(LabeledContent(
        contentIid = content1.iid,
        labelIid = conn1to2.metaLabelIid
      ))

      val expected = DistributedQueryService.ResponseData(None, Some(conn2to1.iid), "content", Some(List(content1.toJson))).toJson
      client2.distributedQuery[Content](s"1 = 1", Nil, List(conn2to1), context="zee_query")(handleAsyncResponse(_, expected, p1))
      
      
      Await.result(p1.future, Duration("30 seconds"))

      logger.debug("QueryConnMetaLabelIntegrator: PASS")
    } catch {
      case e: Exception => logger.warn("QueryConnMetaLabelIntegrator: FAIL", e)
    }
  }

  def handleAsyncResponse(
    response: AsyncResponse,
    expected: JValue,
    p: Promise[Unit]
  ): Unit = {
    response.responseType match {
      case AsyncResponseType.Query =>
        logger.debug(s"Async Response Data -- ${response}")
        if (response.data == expected) {
          p.success()
        } else {
          p.failure(new Exception(s"Response data not as expected\nReceived:\n${response.data.toJsonStr}\nExpected:\n${expected.toJsonStr}"))
        }
      case _ =>
    }
  }
  
  def createSampleContent(client: ChannelClient, alias: Alias, aclConnection: Option[Connection]): (List[Content], List[Label]) = {
    val l_a = client.createChildLabel(alias.rootLabelIid, "A")
    val l_b = client.createChildLabel(l_a.iid, "B")
    val l_c = client.createChildLabel(l_b.iid, "C")
    
    val labels = List(l_a, l_b, l_c)
    var contents = List.empty[Content]
    
    labels.foreach { l =>
      val content = client.upsert(Content(
        aliasIid = alias.iid,
        contentType = "text",
        data = ("text" ->  l.name) ~ ("booyaka" -> "wop")
      ))

      client.upsert(LabeledContent(
        contentIid = content.iid,
        labelIid = l.iid
      ))

      contents = content :: contents
    }

    aclConnection.foreach { connection =>
      client.upsert(LabelAcl(
        connectionIid = connection.iid,
        labelIid = l_a.iid
      ))
    }
    
    (contents.reverse, labels)
  }
}
