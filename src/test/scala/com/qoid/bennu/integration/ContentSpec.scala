package com.qoid.bennu.integration

import com.qoid.bennu.JsonAssist._
import com.qoid.bennu.JsonAssist.jsondsl._
import com.qoid.bennu.client._
import com.qoid.bennu.model.id.SemanticId
import org.specs2.Specification
import org.specs2.execute.Result
import scala.async.Async

class ContentSpec extends Specification {
  implicit val config = HttpClientConfig()

  def is = s2"""
    ${section("integration")}

    Content should
      create content            ${createContent()}
      create semantic content   ${createSemanticContent()}
      update content            ${updateContent()}
      add content label         ${addContentLabel()}
      remove content label      ${removeContentLabel()}

    ${section("integration")}
  """

  //TODO: Test error codes coming back from service
  //TODO: Test sending to another agent
  //TODO: Test DoV

  def createContent(): Result = {
    ClientAssist.channelClient1 { client =>
      Async.async {
        val label = Async.await(client.getCurrentAliasLabel())
        val data: JValue = "text" -> "My content"
        val content = Async.await(client.createContent("TEXT", data, List(label.iid)))

        content.data must_== data
      }
    }.await(60)
  }

  def createSemanticContent(): Result = {
    ClientAssist.channelClient1 { client =>
      Async.async {
        val label = Async.await(client.getCurrentAliasLabel())
        val data: JValue = "text" -> "My content"
        val semanticId = SemanticId.random
        val content = Async.await(client.createContent("TEXT", data, List(label.iid), Some(semanticId)))

        (content.data must_== data) and (content.semanticId must_== Some(semanticId))
      }
    }.await(60)
  }

  def updateContent(): Result = {
    ClientAssist.channelClient1 { client =>
      Async.async {
        val label = Async.await(client.getCurrentAliasLabel())
        val content = Async.await(client.createContent("TEXT", "text" -> "My content", List(label.iid)))
        val data: JValue = "text" -> "My updated content"
        val content2 = Async.await(client.updateContent(content.iid, data))

        content2.data must_== data
      }
    }.await(60)
  }

  def addContentLabel(): Result = {
    ClientAssist.channelClient1 { client =>
      Async.async {
        val label = Async.await(client.getCurrentAliasLabel())
        val label2 = Async.await(client.createLabel(label.iid, "Test"))
        val content = Async.await(client.createContent("TEXT", "text" -> "My content", List(label.iid)))
        val contentLabel = Async.await(client.addContentLabel(content.iid, label2.iid))

        (contentLabel.contentIid must_== content.iid) and (contentLabel.labelIid must_== label2.iid)
      }
    }.await(60)
  }

  def removeContentLabel(): Result = {
    ClientAssist.channelClient1 { client =>
      Async.async {
        val label = Async.await(client.getCurrentAliasLabel())
        val content = Async.await(client.createContent("TEXT", "text" -> "My content", List(label.iid)))
        val contentLabel = Async.await(client.removeContentLabel(content.iid, label.iid))

        (contentLabel.contentIid must_== content.iid) and (contentLabel.labelIid must_== label.iid)
      }
    }.await(60)
  }
}
