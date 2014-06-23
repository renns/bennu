package com.qoid.bennu.webservices

import com.google.inject.Inject
import com.qoid.bennu.JsonAssist._
import com.qoid.bennu.JsonAssist.jsondsl._
import com.qoid.bennu.model.id.AuthenticationId
import com.qoid.bennu.session.SessionManager
import java.sql.{ Connection => JdbcConn }
import m3.predef._
import m3.predef.box._
import m3.servlet.ForbiddenException
import m3.servlet.beans.Parm

case class Login @Inject() (
  sessionMgr: SessionManager,
  @Parm authenticationId: AuthenticationId,
  @Parm password: String = "password" //TODO: Remove default value
) {

  implicit val jdbcConn = inject[JdbcConn]

  def service: JValue = {
    sessionMgr.createSession(authenticationId, password) match {
      case Full(session) => ("channelId" -> session.channel.id.value) ~ ("aliasIid" -> session.aliasIid)
      case _ => throw new ForbiddenException("authentication failed")
    }
  }
}
