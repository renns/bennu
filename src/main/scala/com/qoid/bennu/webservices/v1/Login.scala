package com.qoid.bennu.webservices.v1

import com.google.inject.Inject
import com.qoid.bennu.JsonAssist._
import com.qoid.bennu.JsonAssist.jsondsl._
import com.qoid.bennu.model.id.AuthenticationId
import com.qoid.bennu.security.SecurityContext
import com.qoid.bennu.session.SessionManager
import com.qoid.bennu.BennuException
import com.qoid.bennu.ErrorCode
import m3.Txn
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
      val (session, alias) = sessionMgr.createSession(authenticationId, password)

      Txn {
        Txn.setViaTypename[SecurityContext](session.securityContext)
        ("channelId" -> session.channel.id.value) ~ ("alias" -> alias.toJson)
      }
    } catch {
      case e: BennuException =>
        if (e.getErrorCode() == ErrorCode.authenticationFailed) {
          throw new HttpResponseException(HttpStatusCodes.FORBIDDEN, e.getErrorCode()).initCause(e)
        } else {
          throw new HttpResponseException(HttpStatusCodes.BAD_REQUEST, e.getErrorCode()).initCause(e)
        }
    }
  }
}
