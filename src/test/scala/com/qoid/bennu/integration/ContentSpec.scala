package com.qoid.bennu.integration

import com.qoid.bennu.JsonAssist._
import com.qoid.bennu.JsonAssist.jsondsl._
import com.qoid.bennu.client._
import org.specs2.Specification
import org.specs2.execute.Result
import scala.async.Async

class ContentSpec extends Specification {
  implicit val config = HttpClientConfig()

  def is = s2"""
    ${section("integration")}

    Content should
      create content      ${createContent()}

    ${section("integration")}
  """

  //TODO: Test error codes coming back from service
  //TODO: Test sending to another agent
  //TODO: Test DoV

  def createContent(): Result = {
    ClientAssist.channelClient1 { client =>
      Async.async {
        val rootLabel = Async.await(client.getRootLabel())
        val data: JValue = "text" -> "My content"
        val content = Async.await(client.createContent("TEXT", data, List(rootLabel.iid)))

        content.data must_== data
      }
    }.await(60)
  }
}
