package com.qoid.bennu.distributed.handlers

import com.qoid.bennu.distributed.QueryResponseManager
import com.qoid.bennu.distributed.messages._
import com.qoid.bennu.model.Connection
import net.codingwell.scalaguice.InjectorExtensions.ScalaInjector

object QueryResponseHandler {
  def handle(connection: Connection, queryResponse: QueryResponse, injector: ScalaInjector): Unit = {
    injector.instance[QueryResponseManager].notify(connection, queryResponse)
  }
}
