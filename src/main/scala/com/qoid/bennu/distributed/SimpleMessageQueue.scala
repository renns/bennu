package com.qoid.bennu.distributed

import com.google.inject.Singleton
import com.qoid.bennu.distributed.messages.DistributedMessage
import com.qoid.bennu.model.Connection
import com.qoid.bennu.model.id.PeerId
import m3.LockFreeMap

@Singleton
class SimpleMessageQueue extends MessageQueue {
  private val map = new LockFreeMap[(PeerId, PeerId), DistributedMessage => Unit]

  override def subscribe(connections: List[Connection], fn: Connection => DistributedMessage => Unit): Unit = {
    connections.foreach(c => map.put((c.remotePeerId, c.localPeerId), fn(c)))
  }

  override def unsubscribe(connection: Connection): Unit = {
    map.remove((connection.remotePeerId, connection.localPeerId))
  }

  override def enqueue(connection: Connection, message: DistributedMessage): Unit = {
    map.get((connection.localPeerId, connection.remotePeerId)).foreach(_(message))
  }
}
