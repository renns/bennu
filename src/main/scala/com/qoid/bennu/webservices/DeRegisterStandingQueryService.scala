package com.qoid.bennu.webservices

import com.google.inject.Inject
import com.qoid.bennu.distributed.DistributedManager
import com.qoid.bennu.distributed.messages.DeRegisterStandingQuery
import com.qoid.bennu.distributed.messages.DistributedMessage
import com.qoid.bennu.distributed.messages.DistributedMessageKind
import com.qoid.bennu.model._
import com.qoid.bennu.security.AgentView
import com.qoid.bennu.squery._
import m3.jdbc._
import m3.predef._
import m3.servlet.beans.Parm
import net.liftweb.json._
import scala.language.existentials

case class DeRegisterStandingQueryService @Inject()(
  injector: ScalaInjector,
  sQueryMgr: StandingQueryManager,
  distributedMgr: DistributedManager,
  @Parm handle: Handle
) extends Logging {

  def service: JValue = {
    val av = injector.instance[AgentView]

    sQueryMgr.remove(handle)

    val connectionIids = sQueryMgr.getConnectionIids(handle)

    val request = DeRegisterStandingQuery(handle)

    connectionIids.foreach { connectionIid =>
      av.select[Connection](sql"iid = $connectionIid").foreach { c =>
        distributedMgr.send(c, DistributedMessage(DistributedMessageKind.DeRegisterStandingQuery, 1, request.toJson))
      }
    }

    JString("success")
  }
}
