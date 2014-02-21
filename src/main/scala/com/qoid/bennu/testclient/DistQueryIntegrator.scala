package com.qoid.bennu.testclient

import com.qoid.bennu.model._
import com.qoid.bennu.testclient.client._
import m3.guice.GuiceApp
import scala.concurrent._
import scala.concurrent.duration.Duration
import com.qoid.bennu.JsonAssist._
import com.qoid.bennu.JsonAssist.jsondsl._
import com.qoid.bennu.webservices.DistributedQueryService

object DistQueryIntegrator extends GuiceApp {
  implicit val config = HttpAssist.HttpClientConfig()

  run()
  System.exit(0)

  def run(): Unit = {
    try {
      val p1 = Promise[Unit]()
      val p2 = Promise[Unit]()

      val client1 = HttpAssist.createAgent(AgentId("Agent1"))
      val client2 = HttpAssist.createAgent(AgentId("Agent2"))
      val alias1 = client1.getUberAlias()
      val alias2 = client2.getUberAlias()
      val (conn1, conn2) = TestAssist.createConnection(client1, alias1, client2, alias2)

      val (contents, labels) = createSampleContent(client2, alias2, conn2)
      val content_c = contents(2)
      val label_c = labels.last

      val expected = DistributedQueryService.ResponseData(conn1.iid, "Content", Some(List(content_c.toJson))).toJson
      client1.distributedQuery[Content](s"hasLabelPath('A','B','C')", List(conn1))(handleAsyncResponse(_, expected, p1))
      client1.distributedQuery[Content](s"hasLabel('${label_c.iid.value}')", List(conn1))(handleAsyncResponse(_, expected, p2))
      
      Await.result(p1.future, Duration("30 seconds"))
      Await.result(p2.future, Duration("30 seconds"))

      logger.debug("DistQueryIntegrator: PASS")
    } catch {
      case e: Exception => logger.warn("DistQueryIntegrator: FAIL -- ", e)
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
  
  def createSampleContent(client: ChannelClient, alias: Alias, connection: Connection): (List[Content], List[Label]) = {
    val l_a = client.createChildLabel(alias.rootLabelIid, "A")
    val l_b = client.createChildLabel(l_a.iid, "B")
    val l_c = client.createChildLabel(l_b.iid, "C")
    
    val labels = List(l_a, l_b, l_c)
    var contents = List.empty[Content]
    
    labels.foreach { l =>
      val content = client.upsert(Content(
        agentId = client.agentId,
        aliasIid = alias.iid,
        contentType = "text",
        data = ("text" ->  l.name) ~ ("booyaka" -> "wop")
      ))

      client.upsert(LabeledContent(
        agentId = client.agentId,
        contentIid = content.iid,
        labelIid = l.iid
      ))

      contents = content :: contents
    }

    client.upsert(LabelAcl(
      agentId = client.agentId,
      connectionIid = connection.iid,
      labelIid = l_a.iid
    ))
    
    (contents.reverse, labels)
  }
}
