package com.qoid.bennu.distributed

import com.google.inject.Singleton
import com.qoid.bennu.distributed.messages.DistributedMessage
import com.qoid.bennu.model.Connection
import com.qoid.bennu.model.PeerId
import m3.LockFreeMap

@Singleton
class SimpleMessageQueue {
  private val map = new LockFreeMap[(PeerId, PeerId), DistributedMessage => Unit]

  def subscribe(connections: List[Connection], fn: Connection => DistributedMessage => Unit): Unit = {
    connections.foreach(c => map.put((c.remotePeerId, c.localPeerId), fn(c)))
  }

  def unsubscribe(connection: Connection): Unit = {
    map.remove((connection.remotePeerId, connection.localPeerId))
  }

  def enqueue(connection: Connection, message: DistributedMessage): Unit = {
    map.get((connection.localPeerId, connection.remotePeerId)).foreach(_(message))
  }
}
