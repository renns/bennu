package com.qoid.bennu.testclient

import com.qoid.bennu.model._
import com.qoid.bennu.squery.StandingQueryAction
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
      val p = Promise[Unit]()

      val client1 = HttpAssist.createAgent(AgentId("Agent1"))
      val client2 = HttpAssist.createAgent(AgentId("Agent2"))
      val alias1 = client1.getUberAlias()
      val alias2 = client2.getUberAlias()
      val (conn1, _) = TestAssist.createConnection(client1, alias1, client2, alias2)

      val labels1 = createSampleContent(client1, alias1)
      val labels2 = createSampleContent(client2, alias2)

      val label1_c = labels1.last
      
      client1.query[Content](s"hasLabelPath('A','B')")
      client1.query[Content](s"hasLabel('${label1_c.iid.value}')")
      
      Await.result(p.future, Duration("30 seconds"))

      logger.debug("DistQueryIntegrator: PASS")
    } catch {
      case e: Exception => logger.warn("DistQueryIntegrator: FAIL -- ", e)
    }
  }
  
  def createSampleContent(cl: ChannelClient, alias: Alias): List[Label] = {
    val l_a = cl.createChildLabel(alias.rootLabelIid, "A")
    val l_b = cl.createChildLabel(l_a.iid, "B")
    val l_c = cl.createChildLabel(l_b.iid, "C")
    
    val labels = List(l_a, l_b, l_c)
    
    labels.foreach { l => 
      
      val content = cl.upsert(Content(
        agentId = cl.agentId,
        aliasIid = alias.iid,
        contentType = "text",
        data = ("text" ->  l.name) ~ ("booyaka" -> "wop")
      ))
      
      cl.upsert(LabeledContent(
        agentId = cl.agentId,
        contentIid = content.iid,
        labelIid = l.iid
      ))
      
    }
    
    labels
    
  }

}
