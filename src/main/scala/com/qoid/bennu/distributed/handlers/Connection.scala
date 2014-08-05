package com.qoid.bennu.distributed.handlers

import com.qoid.bennu.JsonAssist._
import com.qoid.bennu.JsonAssist.jsondsl._
import com.qoid.bennu.distributed.DistributedMessage
import com.qoid.bennu.distributed.DistributedMessageKind
import com.qoid.bennu.distributed.DistributedRequestHandler
import com.qoid.bennu.distributed.DistributedResponseHandler
import com.qoid.bennu.distributed.messages
import com.qoid.bennu.model.Connection
import m3.predef._

object DeleteConnectionRequest extends DistributedRequestHandler[messages.DeleteConnectionRequest] {
  override protected val requestKind = DistributedMessageKind.DeleteConnectionRequest
  override protected val responseKind = DistributedMessageKind.DeleteConnectionResponse
  override protected val allowedVersions = List(1)

  override def process(message: DistributedMessage, request: messages.DeleteConnectionRequest, injector: ScalaInjector): JValue = {
    val connection = Connection.fetch(request.connectionIid)
    Connection.delete(connection)
    messages.DeleteConnectionResponse(request.connectionIid).toJson
  }
}

object DeleteConnectionResponse extends DistributedResponseHandler[messages.DeleteConnectionResponse] {
  override protected val responseKind = DistributedMessageKind.DeleteConnectionResponse
  override protected val allowedVersions = List(1)

  override protected def getServiceResult(response: messages.DeleteConnectionResponse): JValue = {
    "connectionIid" -> response.connectionIid
  }
}
