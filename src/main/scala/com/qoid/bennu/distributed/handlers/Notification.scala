package com.qoid.bennu.distributed.handlers

import com.qoid.bennu.JsonAssist._
import com.qoid.bennu.JsonAssist.jsondsl._
import com.qoid.bennu.distributed.DistributedMessage
import com.qoid.bennu.distributed.DistributedMessageKind
import com.qoid.bennu.distributed.DistributedRequestHandler
import com.qoid.bennu.distributed.DistributedResponseHandler
import com.qoid.bennu.distributed.messages
import com.qoid.bennu.model.Notification
import m3.predef._

object ConsumeNotificationRequest extends DistributedRequestHandler[messages.ConsumeNotificationRequest] {
  override protected val requestKind = DistributedMessageKind.ConsumeNotificationRequest
  override protected val responseKind = DistributedMessageKind.ConsumeNotificationResponse
  override protected val allowedVersions = List(1)

  override def process(message: DistributedMessage, request: messages.ConsumeNotificationRequest, injector: ScalaInjector): JValue = {
    val notification = Notification.fetch(request.notificationIid)
    Notification.update(notification.copy(consumed = true))
    messages.ConsumeNotificationResponse(request.notificationIid).toJson
  }
}

object ConsumeNotificationResponse extends DistributedResponseHandler[messages.ConsumeNotificationResponse] {
  override protected val responseKind = DistributedMessageKind.ConsumeNotificationResponse
  override protected val allowedVersions = List(1)

  override protected def getServiceResult(response: messages.ConsumeNotificationResponse, message: DistributedMessage): JValue = {
    "notificationIid" -> response.notificationIid
  }
}
