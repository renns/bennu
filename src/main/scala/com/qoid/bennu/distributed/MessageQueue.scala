package com.qoid.bennu.distributed

import com.qoid.bennu.distributed.messages.DistributedMessage
import com.qoid.bennu.model.Connection

trait MessageQueue {
  def subscribe(connections: List[Connection], fn: Connection => DistributedMessage => Unit): Unit
  def unsubscribe(connection: Connection): Unit
  def enqueue(connection: Connection, message: DistributedMessage): Unit
}
