package com.qoid.bennu.webservices.v1

import com.google.inject.Inject
import com.qoid.bennu.BennuException
import com.qoid.bennu.ErrorCode
import com.qoid.bennu.JsonAssist._
import com.qoid.bennu.distributed.DistributedManager
import com.qoid.bennu.distributed.RequestData
import com.qoid.bennu.distributed.messages.CreateLabelRequest
import com.qoid.bennu.distributed.messages.DistributedMessage
import com.qoid.bennu.distributed.messages.DistributedMessageKind
import com.qoid.bennu.model.id.InternalId
import com.qoid.bennu.session.Session
import m3.predef._
import m3.servlet.HttpResponseException
import m3.servlet.HttpStatusCodes
import m3.servlet.beans.MultiRequestHandler.MethodInvocation
import m3.servlet.beans.Parm

/**
* Creates a new label.
*
* Parameters:
* - route: Array of Strings
* - parentLabelIid: String
* - name: String
* - data: JSON (optional)
*
* Response Values: None
*
* Error Codes:
* - routeInvalid
* - nameInvalid
*/
case class CreateLabel @Inject()(
  session: Session,
  distributedMgr: DistributedManager,
  methodInvocation: MethodInvocation,
  @Parm route: List[InternalId],
  @Parm parentLabelIid: InternalId,
  @Parm name: String,
  @Parm data: JValue = JNothing
) extends Logging {

  def doPost(): Unit = {
    try {
      validateParameters()

      val createLabelRequest = CreateLabelRequest(parentLabelIid, name, data)
      val message = DistributedMessage(DistributedMessageKind.CreateLabelRequest, 1, route, createLabelRequest.toJson)
      val requestData = RequestData(session.channel.id, methodInvocation.context, true)

      distributedMgr.sendRequest(message, requestData)
    } catch {
      case e: BennuException =>
        throw new HttpResponseException(HttpStatusCodes.BAD_REQUEST, e.getErrorCode()).initCause(e)
    }
  }

  private def validateParameters(): Unit = {
    if (route.isEmpty) {
      throw new BennuException(ErrorCode.routeInvalid)
    }

    if (name.isEmpty) {
      throw new BennuException(ErrorCode.nameInvalid)
    }
  }
}
