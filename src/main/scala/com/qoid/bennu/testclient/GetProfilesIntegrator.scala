package com.qoid.bennu.testclient

import com.qoid.bennu.JsonAssist._
import com.qoid.bennu.model._
import com.qoid.bennu.testclient.client._
import com.qoid.bennu.webservices.QueryService
import m3.guice.GuiceApp
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Promise}

object GetProfilesIntegrator extends GuiceApp {
  implicit val config = HttpAssist.HttpClientConfig()

  run()
  System.exit(0)

  def run(): Unit = {
    try {
      val p = Promise[Unit]()

      val client1 = HttpAssist.createAgent("Agent1")
      val client2 = HttpAssist.createAgent("Agent2")
      val alias1 = client1.getRootAlias()
      val label2 = client2.getRootLabel()
      val alias2 = client2.createAlias(label2.iid, "Test")
      val (conn12, _) = TestAssist.createConnection(client1, alias1, client2, alias2)

      val expected = QueryService.ResponseData(None, Some(conn12.iid), "profile", None, JNothing).toJson
      client1.query[TestAssist.Profile]("", None, List(conn12))(handleAsyncResponse(_, expected, p))

      Await.result(p.future, Duration("30 seconds"))

      logger.debug("GetProfiles: PASS")
    } catch {
      case e: Exception => logger.warn("GetProfiles: FAIL", e)
    }

    def handleAsyncResponse(
      response: AsyncResponse,
      expected: JValue,
      p: Promise[Unit]
    ): Unit = {
      response.responseType match {
        case AsyncResponseType.Query =>
          logger.debug(s"Async Response Data -- ${response.data}")
          if (response.data == expected) {
            p.success()
          }
        case _ =>
      }
    }
  }
}
