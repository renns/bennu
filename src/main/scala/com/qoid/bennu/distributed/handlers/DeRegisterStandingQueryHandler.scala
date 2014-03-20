package com.qoid.bennu.distributed.handlers

import com.qoid.bennu.distributed.messages.DeRegisterStandingQuery
import com.qoid.bennu.model._
import com.qoid.bennu.squery.StandingQueryManager
import m3.predef._

object DeRegisterStandingQueryHandler extends Logging {
  def handle(connection: Connection, deRegisterStandingQuery: DeRegisterStandingQuery, injector: ScalaInjector): Unit = {
    val sQueryMgr = injector.instance[StandingQueryManager]
    sQueryMgr.remove(deRegisterStandingQuery.handle)
  }
}
