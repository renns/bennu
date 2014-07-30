package com.qoid.bennu.webservices.v1

import javax.servlet.http.HttpServletRequest

import com.google.inject.Inject
import com.qoid.bennu.session.Session
import com.qoid.bennu.session.SessionManager
import m3.predef._

case class Logout @Inject()(
  req: HttpServletRequest,
  sessionMgr: SessionManager,
  session: Session
) extends Logging {

  def doPost(): Unit = {
    val channel = session.channel

    channel.synchronized {
      // complete any existing continuation
      channel.continuationTerminateOtherPolls(req)
    }

    sessionMgr.closeSession(session.channel.id)
  }
}
