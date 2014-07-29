package com.qoid.bennu.webservices.v1

import com.google.inject.Inject
import com.qoid.bennu.BennuException
import com.qoid.bennu.ErrorCode
import com.qoid.bennu.JsonAssist._
import com.qoid.bennu.distributed.DistributedManager
import com.qoid.bennu.distributed.DistributedMessage
import com.qoid.bennu.distributed.DistributedMessageKind
import com.qoid.bennu.distributed.RequestData
import com.qoid.bennu.distributed.messages.CreateContentRequest
import com.qoid.bennu.model.id.InternalId
import com.qoid.bennu.session.Session
import m3.predef._
import m3.servlet.HttpResponseException
import m3.servlet.HttpStatusCodes
import m3.servlet.beans.MultiRequestHandler.MethodInvocation
import m3.servlet.beans.Parm

/**
* Creates a new piece of content.
*
* Parameters:
* - route: Array of Strings
* - contentType: String
* - data: JSON
* - labelIids: Array of Strings
*
* Response Values: None
*
* Error Codes:
* - routeInvalid
* - contentTypeInvalid
* - dataInvalid
* - labelIidsInvalid
*/
case class CreateContent @Inject()(
  session: Session,
  distributedMgr: DistributedManager,
  methodInvocation: MethodInvocation,
  @Parm route: List[InternalId],
  @Parm contentType: String,
  @Parm data: JValue,
  @Parm labelIids: List[InternalId]
) extends Logging {

  def doPost(): Unit = {
    try {
      validateParameters()

      val createContentRequest = CreateContentRequest(contentType, data, labelIids)
      val message = DistributedMessage(DistributedMessageKind.CreateContentRequest, 1, route, createContentRequest.toJson)
      val requestData = RequestData(session.channel.id, methodInvocation.context, true)

      distributedMgr.sendRequest(message, requestData)
    } catch {
      case e: BennuException =>
        throw new HttpResponseException(HttpStatusCodes.BAD_REQUEST, e.getErrorCode()).initCause(e)
    }
  }

  private def validateParameters(): Unit = {
    if (route.isEmpty) throw new BennuException(ErrorCode.routeInvalid)
    if (contentType.isEmpty) throw new BennuException(ErrorCode.contentTypeInvalid)
    if (data == JNothing) throw new BennuException(ErrorCode.dataInvalid)
    if (labelIids.isEmpty) throw new BennuException(ErrorCode.labelIidsInvalid)
  }
}
