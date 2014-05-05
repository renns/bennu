package com.qoid.bennu.testclient.client

import com.qoid.bennu.JdbcAssist
import com.qoid.bennu.JsonAssist._
import com.qoid.bennu.JsonAssist.jsondsl._
import com.qoid.bennu.model._
import com.qoid.bennu.model.id._
import com.qoid.bennu.model.notification.NotificationKind
import com.qoid.bennu.squery.StandingQueryAction
import com.qoid.bennu.testclient.client.HttpAssist.HttpClientConfig
import m3.jdbc._
import m3.predef._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent._

object TestAssist extends Logging {

  def createConnection(
    clientA: ChannelClient,
    aliasAIid: InternalId,
    clientB: ChannelClient,
    aliasBIid: InternalId
  ): (Connection, Connection) = {

    val peerId1 = PeerId.random
    val peerId2 = PeerId.random
    val connAB = clientA.createConnection(aliasAIid, peerId1, peerId2)
    val connBA = clientB.createConnection(aliasBIid, peerId2, peerId1)

    (connAB, connBA)
  }

  def createAndConnectAgents(agentNames: List[String])(implicit config: HttpClientConfig): Unit = {
    val clients = agentNames.map(HttpAssist.createAgent)
    connectAgents(clients)
  }

  def connectAgents(clients: List[ChannelClient]): Unit = {
    clients match {
      case client1 :: tail =>
        tail.foreach(client2 => TestAssist.createConnection(client1, client1.rootAliasIid, client2, client2.rootAliasIid))
        connectAgents(tail)
      case _ =>
    }
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
      val content = client.createContent(
        alias.iid,
        "TEXT",
        ("text" -> l.name) ~ ("booyaka" -> "wop"),
        List(l.iid)
      )

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

  def doIntroduction(
    clientI: ChannelClient,
    clientA: ChannelClient,
    clientB: ChannelClient,
    connIA: Connection,
    connIB: Connection
  ): Future[(Connection, Connection)] = {

    val pA = Promise[Connection]()
    val pB = Promise[Connection]()

    subscribeInsert[Notification](clientA, sql"kind = ${NotificationKind.IntroductionRequest.toString}") { (notification, handle) =>
      clientA.deRegisterStandingQuery(handle)

      subscribeInsert[Connection](clientA) { (connection, handle) =>
        clientA.deRegisterStandingQuery(handle)
        pA.success(connection)
      }

      clientA.respondToIntroduction(notification, true)
    }

    subscribeInsert[Notification](clientB, sql"kind = ${NotificationKind.IntroductionRequest.toString}") { (notification, handle) =>
      clientB.deRegisterStandingQuery(handle)

      subscribeInsert[Connection](clientB) { (connection, handle) =>
        clientB.deRegisterStandingQuery(handle)
        pB.success(connection)
      }

      // Put a delay to prevent race condition. This can be removed once race condition is fixed.
      Thread.sleep(1000)
      clientB.respondToIntroduction(notification, true)
    }

    clientI.initiateIntroduction(connIA, "Message to A", connIB, "Message to B")

    for (cA <- pA.future; cB <- pB.future) yield (cA, cB)
  }

  def subscribeInsert[T <: HasInternalId : Manifest](client: ChannelClient, query: String = "")(fn: (T, Handle) => Unit): Unit = {
    client.query[T](query, historical = false, standing = true) {
      case QueryResponse(QueryResponseType.SQuery, handle, tpe, _, JArray(i :: Nil), _, _, Some(StandingQueryAction.Insert)) if tpe =:= JdbcAssist.findMapperByType[T].typeName =>
        val mapper = JdbcAssist.findMapperByType[T]
        val instance = mapper.fromJson(i)
        fn(instance, handle)
    }
  }
}
