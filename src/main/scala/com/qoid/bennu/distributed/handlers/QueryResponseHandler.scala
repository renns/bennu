package com.qoid.bennu.distributed.handlers

import com.qoid.bennu.MemoryCache
import com.qoid.bennu.distributed.DistributedManager
import com.qoid.bennu.distributed.messages._
import com.qoid.bennu.model.Connection
import com.qoid.bennu.model.Handle
import m3.predef._

object QueryResponseHandler {
  private val cache = new MemoryCache[Handle, (Connection, QueryResponse) => Unit]

  def registerHandle(handle: Handle, fn: (Connection, QueryResponse) => Unit): Unit = {
    cache.put(handle, fn)
  }

  def deRegisterHandle(handle: Handle): Unit = {
    cache.remove(handle)
  }

  def handle(connection: Connection, message: DistributedMessage): Unit = {
    message.version match {
      case 1 => process(connection, message)
      case _ => inject[DistributedManager].sendNotSupported(connection)
    }
  }

  private def process(connection: Connection, message: DistributedMessage): Unit = {
    val data = QueryResponse.fromJson(message.data)
    cache.get(data.handle).foreach(_(connection, data))
  }
}
