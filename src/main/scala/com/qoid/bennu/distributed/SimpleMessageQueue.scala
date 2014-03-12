package com.qoid.bennu.distributed

import com.qoid.bennu.distributed.messages.DistributedMessage
import com.qoid.bennu.model.Connection
import com.qoid.bennu.model.PeerId
import m3.LockFreeMap

@com.google.inject.Singleton
class SimpleMessageQueue {
  private val map = new LockFreeMap[(PeerId, PeerId), DistributedMessage => Unit]

  def subscribe(connections: List[Connection], fn: Connection => DistributedMessage => Unit): Unit = {
    connections.foreach(c => map += (c.remotePeerId, c.localPeerId) -> fn(c))
  }

  def enqueue(connection: Connection, message: DistributedMessage): Unit = {
    map.get((connection.localPeerId, connection.remotePeerId)).foreach(_(message))
  }
}
