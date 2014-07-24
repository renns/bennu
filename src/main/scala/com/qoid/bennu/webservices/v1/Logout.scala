package com.qoid.bennu.webservices.v1

import javax.servlet.http.HttpServletRequest

import com.google.inject.Inject
import com.qoid.bennu.session.Session
import com.qoid.bennu.session.SessionManager
import m3.predef._

/**
* Logs out of the current session.
*
* Call this service directly and not by submitting on the channel.
*
* Parameters: None
* Response Values: None
* Error Codes: None
*/
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
