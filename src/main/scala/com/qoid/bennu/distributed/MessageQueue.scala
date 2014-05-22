package com.qoid.bennu.distributed

import com.qoid.bennu.distributed.messages.DistributedMessage
import com.qoid.bennu.model.Connection
import com.qoid.bennu.model.id.InternalId

trait MessageQueue {
  def subscribe(connections: List[Connection], fn: InternalId => DistributedMessage => Unit): Unit
  def unsubscribe(connection: Connection): Unit
  def enqueue(connection: Connection, message: DistributedMessage): Unit
}
