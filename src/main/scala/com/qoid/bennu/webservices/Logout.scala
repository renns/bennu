//package com.qoid.bennu.webservices
//
//import java.sql.{Connection => JdbcConn}
//
//import com.google.inject.Inject
//import com.qoid.bennu.JsonAssist._
//import com.qoid.bennu.session.Session
//import com.qoid.bennu.session.SessionManager
//import m3.predef._
//
//case class Logout @Inject() (
//  sessionMgr: SessionManager,
//  session: Session
//) {
//
//  implicit val jdbcConn = inject[JdbcConn]
//
//  def service: JValue = {
//    sessionMgr.closeSession(session.channel.id)
//    JNothing
//  }
//}
