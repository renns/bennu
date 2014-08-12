package com.qoid.bennu.webservices

import com.qoid.bennu.BennuException
import com.qoid.bennu.ErrorCode
import com.qoid.bennu.JsonAssist._
import com.qoid.bennu.distributed.DistributedManager
import com.qoid.bennu.distributed.DistributedMessage
import com.qoid.bennu.distributed.DistributedMessageKind
import com.qoid.bennu.distributed.RequestData
import com.qoid.bennu.model.id.InternalId
import com.qoid.bennu.session.Session
import m3.predef._
import m3.servlet.HttpResponseException
import m3.servlet.HttpStatusCodes
import m3.servlet.beans.MultiRequestHandler.MethodInvocation

trait DistributedService {
  protected val request: JValue
  protected val singleResponse = true

  protected def run(
    injector: ScalaInjector,
    distributedMessageKind: DistributedMessageKind,
    route: List[InternalId]
  ): Unit = {

    try {
      val session = injector.instance[Session]
      val methodInvocation = injector.instance[MethodInvocation]
      val distributedMgr = injector.instance[DistributedManager]

      if (route.isEmpty) throw new BennuException(ErrorCode.routeInvalid)

      validateParameters()

      val message = DistributedMessage(distributedMessageKind, 1, route, request)
      val requestData = RequestData(session.channel.id, methodInvocation.context, singleResponse)

      beforeSend(message)

      distributedMgr.send(message, requestData)
    } catch {
      case e: BennuException =>
        throw new HttpResponseException(HttpStatusCodes.BAD_REQUEST, e.getErrorCode()).initCause(e)
    }
  }

  protected def validateParameters(): Unit = ()
  protected def beforeSend(message: DistributedMessage): Unit = ()
}
