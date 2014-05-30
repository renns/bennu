package com.qoid.bennu.integration

import com.qoid.bennu.client._
import org.specs2.Specification
import org.specs2.execute.Result
import scala.async.Async

class UpsertSpec extends Specification {
  implicit val config = HttpClientConfig()

  def is = s2"""
    ${section("integration")}

    Upsert should
      insert label    ${insertLabel()}
      update label    ${updateLabel()}

    ${section("integration")}
  """

  def insertLabel(): Result = {
    TestAssist.channelClient1 { client =>
      Async.async {
        val labelName = "Insert Label"
        val rootLabel = Async.await(client.getRootLabel())
        val label = Async.await(client.createLabel(rootLabel.iid, labelName))

        label.name must_== labelName
      }
    }.await(30)
  }

  def updateLabel(): Result = {
    TestAssist.channelClient1 { client =>
      Async.async {
        val labelName = "Update Label"
        val rootLabel = Async.await(client.getRootLabel())
        val label = Async.await(client.createLabel(rootLabel.iid, "Insert Label"))
        val updatedLabel = Async.await(client.updateLabel(label.iid, labelName, label.data))

        updatedLabel.name must_== labelName
      }
    }.await(30)
  }
}
