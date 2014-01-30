package com.qoid.bennu.webservices

import com.google.inject.Inject
import com.qoid.bennu.model._
import com.qoid.bennu.squery._
import m3.predef._
import m3.servlet.beans.Parm
import scala.language.existentials

case class DeRegisterStandingQueryService @Inject()(
  sQueryMgr: StandingQueryManager,
  @Parm handle: InternalId,
  @Parm agentId: AgentId
) extends Logging {

  def service = {
    sQueryMgr.remove(agentId, handle)
    //TODO: Return something
  }
}
