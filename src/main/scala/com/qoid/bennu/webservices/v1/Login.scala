package com.qoid.bennu.webservices.v1

import com.google.inject.Inject
import com.qoid.bennu.BennuException
import com.qoid.bennu.JsonAssist._
import com.qoid.bennu.JsonAssist.jsondsl._
import com.qoid.bennu.model.id.AuthenticationId
import com.qoid.bennu.session.SessionManager
import m3.predef._
import m3.servlet.HttpStatusCodes
import m3.servlet.beans.Parm
import org.apache.http.client.HttpResponseException

case class Login @Inject()(
  sessionMgr: SessionManager,
  @Parm authenticationId: AuthenticationId,
  @Parm password: String
) extends Logging {

  def doPost(): JValue = {
    try {
      val session = sessionMgr.createSession(authenticationId, password)
      ("channelId" -> session.channel.id.value) ~ ("connectionIid" -> session.securityContext.connectionIid)
    } catch {
      case e: BennuException =>
        throw new HttpResponseException(HttpStatusCodes.BAD_REQUEST, e.getErrorCode()).initCause(e)
    }
  }
}
