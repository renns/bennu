package com.qoid.bennu.webservices.v1

import com.google.inject.Inject
import com.qoid.bennu.BennuException
import com.qoid.bennu.session.Session
import m3.servlet.HttpResponseException
import m3.servlet.HttpStatusCodes
import m3.servlet.beans.MultiRequestHandler.MethodInvocation

case class CancelQuery @Inject()(
  session: Session,
  methodInvocation: MethodInvocation
) {

  def doPost(): Unit = {
    try {
      session.cancelStandingQuery(methodInvocation.context)
    } catch {
      case e: BennuException =>
        throw new HttpResponseException(HttpStatusCodes.BAD_REQUEST, e.getErrorCode()).initCause(e)
    }
  }
}
