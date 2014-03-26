package com.qoid.bennu.testclient.client

import com.qoid.bennu.JsonAssist._
import com.qoid.bennu.JsonAssist.jsondsl._
import com.qoid.bennu.model._
import com.qoid.bennu.model.id.PeerId
import m3.predef._
import scala.concurrent.Promise

object TestAssist extends Logging {

  def createConnection(
    clientA: ChannelClient,
    aliasA: Alias,
    clientB: ChannelClient,
    aliasB: Alias
  ): (Connection, Connection) = {

    val peerId1 = PeerId.random
    val peerId2 = PeerId.random
    val connAB = clientA.createConnection(aliasA.iid, peerId1, peerId2)
    val connBA = clientB.createConnection(aliasB.iid, peerId2, peerId1)

    (connAB, connBA)
  }

  def createSampleContent(
    client: ChannelClient,
    alias: Alias,
    aclConnection: Option[Connection]
  ): (List[Content], List[Label]) = {

    val l_a = client.createLabel(alias.rootLabelIid, "A")
    val l_b = client.createLabel(l_a.iid, "B")
    val l_c = client.createLabel(l_b.iid, "C")

    val labels = List(l_a, l_b, l_c)
    var contents = List.empty[Content]

    labels.foreach { l =>
      val content = client.upsert(Content(
        aliasIid = alias.iid,
        contentType = "text",
        data = ("text" ->  l.name) ~ ("booyaka" -> "wop")
      ))

      client.upsert(LabeledContent(
        contentIid = content.iid,
        labelIid = l.iid
      ))

      contents = content :: contents
    }

    aclConnection.foreach { connection =>
      client.upsert(LabelAcl(
        connectionIid = connection.iid,
        labelIid = l_a.iid
      ))
    }

    (contents.reverse, labels)
  }

  def handleQueryResponse(
    response: QueryResponse,
    expectedResults: JValue,
    p: Promise[Unit]
  ): Unit = {
    if (response.results == expectedResults) {
      p.success()
    } else {
      p.failure(new Exception(s"Response results not as expected\nReceived:\n${response.results.toJsonStr}\nExpected:\n${expectedResults.toJsonStr}"))
    }
  }
}
