package com.qoid.bennu.client

import com.qoid.bennu.JsonAssist.jsondsl._
import com.qoid.bennu.model._
import com.qoid.bennu.model.id.InternalId
import com.qoid.bennu.squery.StandingQueryAction
import m3.jdbc._
import m3.predef._
import scala.async.Async._
import scala.concurrent._

object TestAssist extends Logging {

  def channelClient1[T](
    body: ChannelClient => Future[T]
  )(
    implicit
    config: HttpClientConfig,
    ec: ExecutionContext
  ): Future[T] = {
    async {
      val client1 = await(HttpAssist.createAgent())
      val t = await(body(client1))

      // Delete connection on introducer side
//      val introducerConnection = await(client1.getIntroducerConnection())
//      async {
//        val introducerClient = await(ChannelClientFactory.createHttpChannelClient("introducer"))
//        val connections = await(introducerClient.queryLocal[Connection](sql"localPeerId = ${introducerConnection.remotePeerId} and remotePeerId = ${introducerConnection.localPeerId}"))
//        connections.foreach(c => introducerClient.delete[Connection](c.iid))
//        introducerClient.close()
//      }

      // Delete agent
//      async {
//        val agentClient = await(ChannelClientFactory.createHttpChannelClient(client1.agentName))
//        agentClient.deleteAgent()
//        agentClient.close()
//      }

      client1.close()
      t
    }
  }

  def channelClient2[T](
    body: (ChannelClient, ChannelClient) => Future[T]
  )(
    implicit
    config: HttpClientConfig,
    ec: ExecutionContext
  ): Future[T] = {
    channelClient1 { client1 =>
      channelClient1 { client2 =>
        body(client1, client2)
      }
    }
  }

  def channelClient3[T](
    body: (ChannelClient, ChannelClient, ChannelClient) => Future[T]
  )(
    implicit
    config: HttpClientConfig,
    ec: ExecutionContext
  ): Future[T] = {
    channelClient1 { client1 =>
      channelClient2 { (client2, client3) =>
        body(client1, client2, client3)
      }
    }
  }

  def channelClient4[T](
    body: (ChannelClient, ChannelClient, ChannelClient, ChannelClient) => Future[T]
  )(
    implicit
    config: HttpClientConfig,
    ec: ExecutionContext
  ): Future[T] = {
    channelClient1 { client1 =>
      channelClient3 { (client2, client3, client4) =>
        body(client1, client2, client3, client4)
      }
    }
  }

  def createSampleContent(
    client: ChannelClient,
    labelName: String,
    aclConnectionIid: Option[InternalId] = None
  )(
    implicit ec: ExecutionContext
  ): Future[Content] = {
    async {
      val alias = await(client.getRootAlias())
      val label = await(client.createLabel(alias.rootLabelIid, labelName))
      await(Future.sequence(aclConnectionIid.toList.map(iid => client.grantAccess(iid, label.iid))))
      await(client.createContent(alias.iid, "TEXT", "text" -> labelName, List(label.iid)))
    }
  }

//  def handleQueryResponse(
//    response: QueryResponse,
//    expectedResults: JValue,
//    p: Promise[Unit]
//  ): Unit = {
//
//    val rr = response.results.remove {
//      case JField("created", _) => true
//      case JField("modified", _) => true
//      case _ => false
//    }
//
//    val er = expectedResults.remove {
//      case JField("created", _) => true
//      case JField("modified", _) => true
//      case _ => false
//    }
//
//    if (rr == er) {
//      p.success()
//    } else {
//      p.failure(new Exception(s"Response results not as expected\nReceived:\n${rr.toJsonStr}\nExpected:\n${er.toJsonStr}"))
//    }
//  }

  // Multiple introduce calls should not be run in parallel because it listens for any new
  // connections being created. If multiple are run in parallel, one instance may get the
  // new connection from the other instance.
  def introduce(
    clientA: ChannelClient,
    clientB: ChannelClient
  )(
    implicit
    config: HttpClientConfig,
    ec: ExecutionContext
  ): Future[(Connection, Connection)] = {
    async {
      val fClientI = ChannelClientFactory.createHttpChannelClient("Introducer")
      val fConnAI = clientA.getIntroducerConnection()
      val fConnBI = clientB.getIntroducerConnection()

      val clientI = await(fClientI)
      val connAI = await(fConnAI)
      val connBI = await(fConnBI)

      val fConnIA = clientI.queryLocal[Connection](sql"localPeerId = ${connAI.remotePeerId} and remotePeerId = ${connAI.localPeerId}")
      val fConnIB = clientI.queryLocal[Connection](sql"localPeerId = ${connBI.remotePeerId} and remotePeerId = ${connBI.localPeerId}")

      val fConnAB = getStandingQueryResult[Connection](clientA, StandingQueryAction.Insert)
      val fConnBA = getStandingQueryResult[Connection](clientB, StandingQueryAction.Insert)

      val connIA = await(fConnIA).head
      val connIB = await(fConnIB).head

      await(clientI.initiateIntroduction(connIA, "Message to A", connIB, "Message to B"))

      (await(fConnAB), await(fConnBA))
    }
  }

  def getStandingQueryResult[T <: HasInternalId](
    client: ChannelClient,
    action: StandingQueryAction,
    queryStr: String = "",
    aliasIid: Option[InternalId] = None,
    connectionChain: List[InternalId] = Nil
  )(
    implicit
    m: Manifest[T],
    ec: ExecutionContext
  ): Future[T] = {
    val p = Promise[T]()

    try {
      if (connectionChain.isEmpty) {
        client.queryLocal[T](queryStr, aliasIid, Some({ (t, a, handle) =>
          if (a == action) {
            client.deRegisterStandingQuery(handle)
            p.success(t)
          }
          ()
        })).onFailure { case e => p.failure(e)}
      } else {
        client.queryRemote[T](queryStr, connectionChain, aliasIid, Some({ (t, a, handle) =>
          if (a == action) {
            client.deRegisterStandingQuery(handle)
            p.success(t)
          }
          ()
        })).onFailure { case e => p.failure(e)}
      }
    } catch {
      case e: Exception => p.failure(e)
    }

    // Wait for standing query to get set up
    Thread.sleep(500)

    p.future
  }
}
