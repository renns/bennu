//package com.qoid.bennu.webservices
//
//import com.google.inject.Inject
//import com.qoid.bennu.ErrorCode
//import com.qoid.bennu.JsonAssist._
//import com.qoid.bennu.ServiceException
//import com.qoid.bennu.model.Agent
//import com.qoid.bennu.model.assist.AgentManager
//import com.qoid.bennu.security.AgentSecurityContext
//import com.qoid.bennu.security.AgentView
//import com.qoid.bennu.security.SecurityContext
//import m3.Txn
//import m3.predef._
//import m3.servlet.beans.Parm
//
//case class DeleteAgent @Inject() (
//  injector: ScalaInjector,
//  securityContext: SecurityContext,
//  av: AgentView,
//  agentMgr: AgentManager,
//  @Parm exportData: Boolean
//) {
//
//  def service: JValue = {
//    val agent = av.selectOne[Agent]("")
//
//    if (agent.uberAliasIid == securityContext.aliasIid) {
//      Txn {
//        Txn.setViaTypename[SecurityContext](AgentSecurityContext(injector, securityContext.agentId))
//        val data = if (exportData) agentMgr.exportAgent() else JNothing
//        agentMgr.deleteAgent()
//        data
//      }
//    } else {
//      throw new ServiceException("Not allowed in current context", ErrorCode.Forbidden)
//    }
//  }
//}
