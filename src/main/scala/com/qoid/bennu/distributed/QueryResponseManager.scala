package com.qoid.bennu.distributed

import com.google.inject.Singleton
import com.qoid.bennu.MemoryCache
import com.qoid.bennu.distributed.messages.QueryResponse
import com.qoid.bennu.model.Connection
import com.qoid.bennu.model.Handle

@Singleton
class QueryResponseManager {
  private val cache = new MemoryCache[Handle, (Connection, QueryResponse) => Unit]

  def registerHandle(handle: Handle, fn: (Connection, QueryResponse) => Unit): Unit = {
    cache.put(handle, fn)
  }

  def deRegisterHandle(handle: Handle): Unit = {
    cache.remove(handle)
  }

  def notify(connection: Connection, queryResponse: QueryResponse): Unit = {
    cache.get(queryResponse.handle).foreach(_(connection, queryResponse))
  }
}
