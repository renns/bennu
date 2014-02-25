package com.qoid.bennu.testclient

import com.qoid.bennu.model._
import com.qoid.bennu.testclient.client._
import m3.guice.GuiceApp
import scala.concurrent._
import scala.concurrent.duration.Duration
import com.qoid.bennu.JsonAssist._
import com.qoid.bennu.JsonAssist.jsondsl._
import com.qoid.bennu.webservices.DistributedQueryService
import net.model3.lang.TimeDuration

object DistStandingQueryIntegrator extends GuiceApp {
  implicit val config = HttpAssist.HttpClientConfig()

  run()
  System.exit(0)

  def run(): Unit = {
    try {
      val p1 = Promise[Unit]()

      val client1 = HttpAssist.createAgent(AgentId("Agent1"))
      val alias1 = client1.getUberAlias()

      client1.distributedQuery[Content](s"hasLabelPath('A','B','C')", Nil, Nil, context=JString("zee_queeray_no_alias"), leaveStanding=true)(handleAsyncResponse(_, JNothing, p1))
      client1.distributedQuery[Content](s"hasLabelPath('A','B','C')", List(alias1), Nil, context=JString("zee_queeray_alias"), leaveStanding=true)(handleAsyncResponse(_, JNothing, p1))

      new TimeDuration("5 seconds")
      
      val (contents, labels) = createSampleContent(client1, alias1, None)
      val content_c = contents(2)
      val label_c = labels.last

      Await.result(p1.future, Duration("30 seconds"))

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
    logger.debug(s"Async Response Data -- ${response} \n${response.data.toJsonStr}")
  }
  
  def createSampleContent(client: ChannelClient, alias: Alias, aclConnection: Option[Connection]): (List[Content], List[Label]) = {
    val l_a = client.createChildLabel(alias.rootLabelIid, "A")
    val l_b = client.createChildLabel(l_a.iid, "B")
    val l_c = client.createChildLabel(l_b.iid, "C")
    
    val labels = List(l_a, l_b, l_c)
    var contents = List.empty[Content]
    
    labels.foreach { l =>
      
      val contentIid = InternalId.random

      val content = client.upsert(Content(
        iid = contentIid,
        agentId = client.agentId,
        aliasIid = alias.iid,
        contentType = "text",
        data = ("text" ->  l.name) ~ ("booyaka" -> "wop")
      ))
      
      client.upsert(LabeledContent(
        agentId = client.agentId,
        contentIid = contentIid,
        labelIid = l.iid
      ))

      contents = content :: contents
    }

    aclConnection.foreach { connection =>
      client.upsert(LabelAcl(
        agentId = client.agentId,
        connectionIid = connection.iid,
        labelIid = l_a.iid
      ))
    }
    
    (contents.reverse, labels)
  }
}
