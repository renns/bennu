package com.qoid.bennu.testclient.client

import com.qoid.bennu.model._

object TestAssist {
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
}
