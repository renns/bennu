package com.qoid.bennu.client

import com.qoid.bennu.model.id.InternalId

import scala.async.Async.async
import scala.async.Async.await
import scala.concurrent.ExecutionContext
import scala.concurrent.Future

object ClientAssist {
  def channelClient1[T](
    body: ChannelClient => Future[T]
  )(
    implicit
    config: HttpClientConfig,
    ec: ExecutionContext
  ): Future[T] = {
    async {
      val agentName = InternalId.uidGenerator.create(32)
      val password = "test"

      val authenticationId = await(AgentAssist.createAgent(agentName, password))
      val client1 = await(AgentAssist.login(authenticationId, password))

      val t = await(body(client1))

      client1.close()
      await(client1.logout())

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

//  def anonymousClient1[T](
//    body: (ChannelClient) => Future[T]
//  )(
//    implicit
//    config: HttpClientConfig,
//    ec: ExecutionContext
//  ): Future[T] = {
//    channelClient1 { client1 =>
//      async {
//        val alias = await(client1.getAlias("Anonymous"))
//        await(client1.spawnSession(alias.iid) { anonymousClient =>
//          body(anonymousClient)
//        })
//      }
//    }
//  }
//
//  def anonymousClient2[T](
//    body: (ChannelClient, ChannelClient) => Future[T]
//  )(
//    implicit
//    config: HttpClientConfig,
//    ec: ExecutionContext
//  ): Future[T] = {
//    anonymousClient1 { client1 =>
//      anonymousClient1 { client2 =>
//        body(client1, client2)
//      }
//    }
//  }
}
