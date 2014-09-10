package com.qoid.bennu.webservices.v1

import com.google.inject.Inject
import com.qoid.bennu.BennuException
import com.qoid.bennu.JsonAssist._
import com.qoid.bennu.JsonAssist.jsondsl._
import com.qoid.bennu.model.Alias
import com.qoid.bennu.model.id.InternalId
import com.qoid.bennu.security.SecurityContext
import com.qoid.bennu.session.SessionManager
import m3.Txn
import m3.predef._
import m3.servlet.HttpStatusCodes
import m3.servlet.beans.Parm
import org.apache.http.client.HttpResponseException

case class SpawnSession @Inject()(
  sessionMgr: SessionManager,
  @Parm aliasIid: InternalId
) extends Logging {

  def doPost(): JValue = {
    try {
      val session = sessionMgr.createSession(aliasIid)

      Txn {
        Txn.setViaTypename[SecurityContext](session.securityContext)
        val alias = Alias.fetch(session.securityContext.aliasIid)
        ("channelId" -> session.channel.id.value) ~ ("alias" -> alias.toJson)
      }
    } catch {
      case e: BennuException =>
        throw new HttpResponseException(HttpStatusCodes.BAD_REQUEST, e.getErrorCode()).initCause(e)
    }
  }
}
