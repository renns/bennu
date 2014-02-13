package com.qoid.bennu.testclient

import com.qoid.bennu.JsonAssist._
import com.qoid.bennu.JsonAssist.jsondsl._
import com.qoid.bennu.model._
import com.qoid.bennu.testclient.client._
import m3.guice.GuiceApp
import scala.concurrent.{Await, Promise}
import scala.concurrent.duration.Duration

object GetProfilesIntegrator extends GuiceApp {
  implicit val config = HttpAssist.HttpClientConfig()

  run()
  System.exit(0)

  def run(): Unit = {
    try {
      val p = Promise[Unit]()

      val client1 = HttpAssist.createAgent(AgentId("Agent1"))
      val client2 = HttpAssist.createAgent(AgentId("Agent2"))
      val alias1 = client1.getUberAlias()
      val label2 = client2.getUberLabel()
      val alias2 = client2.createAlias(label2.iid, "Test")
      TestAssist.createConnection(client1, alias1, client2, alias2)
      val connections = client1.query[Connection]("")
      val expected = JObject(List(JField("name", "Test"), JField("imgSrc", "")))
      client1.getProfiles(connections)(handleAsyncResponse(_, _, _, expected, p))

      Await.result(p.future, Duration("30 seconds"))

      logger.debug("GetProfiles: PASS")
    } catch {
      case e: Exception => logger.warn("GetProfiles: FAIL -- " + e)
    }

    def handleAsyncResponse(
      responseType: AsyncResponseType,
      handle: InternalId,
      data: JValue,
      expected: JValue,
      p: Promise[Unit]
    ): Unit = {
      responseType match {
        case AsyncResponseType.Profile =>
          logger.debug(s"Async Response Data -- $data")
          if (data == expected) {
            p.success()
          }
        case _ =>
      }
    }
  }
}
