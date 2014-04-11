package com.qoid.bennu.testclient

import com.qoid.bennu.model.id.PeerId
import com.qoid.bennu.testclient.client._
import m3.guice.GuiceApp

object MultiNodeSetup extends GuiceApp {

  // Create a config for each node
  private val config1 = HttpAssist.HttpClientConfig("http://localhost:8081")
  private val config2 = HttpAssist.HttpClientConfig("http://localhost:8082")

  // Connect to each node's Introducer agent
  val client1 = ChannelClientFactory.createHttpChannelClient("Introducer")(config1)
  val client2 = ChannelClientFactory.createHttpChannelClient("Introducer")(config2)

  // Connect Introducer agents together
  connectAgents(client1, client2)

  System.exit(0)

  private def connectAgents(client1: ChannelClient, client2: ChannelClient): Unit = {
    val alias1 = client1.getRootAlias()
    val alias2 = client2.getRootAlias()

    val peerId1 = PeerId.random
    val peerId2 = PeerId.random

    client1.createConnection(alias1.iid, peerId1, peerId2)
    client2.createConnection(alias2.iid, peerId2, peerId1)
  }
}
