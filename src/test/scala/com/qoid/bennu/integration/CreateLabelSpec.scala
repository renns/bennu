package com.qoid.bennu.integration

import com.qoid.bennu.client._
import org.specs2.Specification
import org.specs2.execute.Result

import scala.async.Async

class CreateLabelSpec extends Specification {
  implicit val config = HttpClientConfig()

  def is = s2"""
    ${section("integration")}

    Create Label should
      create a label      ${createLabel()}

    ${section("integration")}
  """

  //TODO: Test error codes coming back from service
  //TODO: Test calling createLabel on another agent
  //TODO: Test DoV

  def createLabel(): Result = {
    ClientAssist.channelClient1 { client =>
      Async.async {
        val rootLabel = Async.await(client.getRootLabel())
        val label = Async.await(client.createLabel(rootLabel.iid, "Label"))

        label.name must_== "Label"
      }
    }.await(60)
  }
}
