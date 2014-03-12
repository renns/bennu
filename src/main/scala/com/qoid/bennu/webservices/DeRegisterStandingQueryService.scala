package com.qoid.bennu.webservices

import com.google.inject.Inject
import com.qoid.bennu.SecurityContext.AgentCapableSecurityContext
import com.qoid.bennu.model._
import com.qoid.bennu.squery._
import m3.predef._
import m3.servlet.beans.Parm
import net.liftweb.json._
import scala.language.existentials

case class DeRegisterStandingQueryService @Inject()(
  sQueryMgr: StandingQueryManager,
  securityContext: AgentCapableSecurityContext,
  @Parm handle: Handle
) extends Logging {

  def service: JValue = {
    sQueryMgr.remove(handle)
    JString("success")
  }
}
