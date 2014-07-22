//package com.qoid.bennu.webservices
//
//import com.google.inject.Inject
//import com.qoid.bennu.JsonAssist._
//import com.qoid.bennu.JsonAssist.jsondsl._
//import com.qoid.bennu.model.id.InternalId
//import com.qoid.bennu.security.AuthenticationManager
//import m3.predef._
//import m3.servlet.beans.Parm
//
//case class CreateLogin @Inject() (
//  injector: ScalaInjector,
//  authenticationMgr: AuthenticationManager,
//  @Parm aliasIid: InternalId,
//  @Parm password: String = "password" //TODO: Remove default value
//) {
//
//  def service: JValue = {
//    val login = authenticationMgr.createLogin(aliasIid, password)
//    "authenticationId" -> login.authenticationId
//  }
//}
