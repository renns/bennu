//package com.qoid.bennu.webservices
//
//import com.google.inject.Inject
//import com.qoid.bennu.JsonAssist._
//import com.qoid.bennu.model.assist.AgentManager
//import m3.servlet.beans.Parm
//
//case class ImportAgent @Inject() (
//  agentMgr: AgentManager,
//  @Parm agentData: JValue
//) {
//
//  def service: JValue = {
//    agentMgr.importAgent(agentData)
//    JString("success")
//  }
//}
