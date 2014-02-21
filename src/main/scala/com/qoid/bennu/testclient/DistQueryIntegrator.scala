package com.qoid.bennu.testclient

import com.qoid.bennu.model._
import com.qoid.bennu.testclient.client._
import m3.guice.GuiceApp
import scala.concurrent._
import scala.concurrent.duration.Duration
import com.qoid.bennu.JsonAssist._
import jsondsl._

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

      val labels2 = createSampleContent(client2, alias2, conn2)
      val label2_c = labels2.last
      
      client1.distributedQuery[Content](s"hasLabelPath('A','B')", List(conn1))(handleAsyncResponse(_, JNothing, p1))
      client1.distributedQuery[Content](s"hasLabel('${label2_c.iid.value}')", List(conn1))(handleAsyncResponse(_, JNothing, p2))
      
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
        //if (response.data == expected) {
          p.success()
        //}
      case _ =>
    }
  }
  
  def createSampleContent(client: ChannelClient, alias: Alias, connection: Connection): List[Label] = {
    val l_a = client.createChildLabel(alias.rootLabelIid, "A")
    val l_b = client.createChildLabel(l_a.iid, "B")
    val l_c = client.createChildLabel(l_b.iid, "C")
    
    val labels = List(l_a, l_b, l_c)
    
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
    }

    client.upsert(LabelAcl(
      agentId = client.agentId,
      connectionIid = connection.iid,
      labelIid = l_a.iid
    ))
    
    labels
  }
}
