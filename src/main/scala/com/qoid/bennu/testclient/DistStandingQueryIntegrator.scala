//package com.qoid.bennu.testclient
//
//import com.qoid.bennu.model._
//import com.qoid.bennu.testclient.client._
//import m3.guice.GuiceApp
//import scala.concurrent._
//import scala.concurrent.duration.Duration
//import com.qoid.bennu.JsonAssist._
//import com.qoid.bennu.JsonAssist.jsondsl._
//import com.qoid.bennu.webservices.DistributedQueryService
//
//object DistStandingQueryIntegrator extends GuiceApp {
//  implicit val config = HttpAssist.HttpClientConfig()
//
//  run()
//  System.exit(0)
//
//  def run(): Unit = {
//    try {
//      val p1 = Promise[Unit]()
//
//      val client1 = HttpAssist.createAgent("Agent1")
//      val alias1 = client1.getRootAlias()
//
//      val context = JString("zee_queeray")
//
//      val expected = DistributedQueryService.ResponseData(Some(alias1.iid), None, "content", Some(List.empty[JValue])).toJson
//      client1.distributedQuery[Content](s"hasLabelPath('uber label','A','B','C')", None, Nil, context=context, standing=true)(handleAsyncResponse(_, expected, p1))
//
//      //TODO: This test isn't working yet. Local standing queries don't seem to be working.
//      createSampleContent(client1, alias1, None)
//
//      Await.result(p1.future, Duration("30 seconds"))
//
//      logger.debug("DistStandingQueryIntegrator: PASS")
//    } catch {
//      case e: Exception => logger.warn("DistStandingQueryIntegrator: FAIL", e)
//    }
//  }
//
//  def handleAsyncResponse(
//    response: AsyncResponse,
//    expected: JValue,
//    p: Promise[Unit]
//  ): Unit = {
//    logger.debug(s"Async Response Data -- ${response}")
//
//    response.responseType match {
//      case AsyncResponseType.SQuery2 =>
//        if (response.data == expected) {
//          p.success()
//        } else {
//          p.failure(new Exception(s"Response data not as expected\nReceived:\n${response.data.toJsonStr}\nExpected:\n${expected.toJsonStr}"))
//        }
//      case _ =>
//    }
//  }
//
//  def createSampleContent(client: ChannelClient, alias: Alias, aclConnection: Option[Connection]): (List[Content], List[Label]) = {
//    val l_a = client.createChildLabel(alias.rootLabelIid, "A")
//    val l_b = client.createChildLabel(l_a.iid, "B")
//    val l_c = client.createChildLabel(l_b.iid, "C")
//
//    val labels = List(l_a, l_b, l_c)
//    var contents = List.empty[Content]
//
//    labels.foreach { l =>
//      val content = client.upsert(Content(
//        aliasIid = alias.iid,
//        contentType = "text",
//        data = ("text" ->  l.name) ~ ("booyaka" -> "wop")
//      ))
//
//      client.upsert(LabeledContent(
//        contentIid = content.iid,
//        labelIid = l.iid
//      ))
//
//      contents = content :: contents
//    }
//
//    aclConnection.foreach { connection =>
//      client.upsert(LabelAcl(
//        connectionIid = connection.iid,
//        labelIid = l_a.iid
//      ))
//    }
//
//    (contents.reverse, labels)
//  }
//}
