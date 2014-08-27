package com.qoid.bennu.distributed.handlers

import com.qoid.bennu.BennuException
import com.qoid.bennu.ErrorCode
import com.qoid.bennu.JsonAssist._
import com.qoid.bennu.JsonAssist.jsondsl._
import com.qoid.bennu.distributed.DistributedMessage
import com.qoid.bennu.distributed.DistributedMessageKind
import com.qoid.bennu.distributed.DistributedRequestHandler
import com.qoid.bennu.distributed.DistributedResponseHandler
import com.qoid.bennu.distributed.messages
import com.qoid.bennu.model.Notification
import m3.predef._

object CreateNotificationRequest extends DistributedRequestHandler[messages.CreateNotificationRequest] {
  override protected val requestKind = DistributedMessageKind.CreateNotificationRequest
  override protected val responseKind = DistributedMessageKind.CreateNotificationResponse
  override protected val allowedVersions = List(1)

  override def process(message: DistributedMessage, request: messages.CreateNotificationRequest, injector: ScalaInjector): JValue = {
    if (request.kind.isEmpty) throw new BennuException(ErrorCode.kindInvalid)

    val notification = Notification.insert(Notification(request.kind, message.replyRoute, data = request.data))
    messages.CreateNotificationResponse(notification).toJson
  }
}

object CreateNotificationResponse extends DistributedResponseHandler[messages.CreateNotificationResponse] {
  override protected val responseKind = DistributedMessageKind.CreateNotificationResponse
  override protected val allowedVersions = List(1)

  override protected def getServiceResult(response: messages.CreateNotificationResponse, message: DistributedMessage): JValue = {
    response.notification.toJson
  }
}

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

object DeleteNotificationRequest extends DistributedRequestHandler[messages.DeleteNotificationRequest] {
  override protected val requestKind = DistributedMessageKind.DeleteNotificationRequest
  override protected val responseKind = DistributedMessageKind.DeleteNotificationResponse
  override protected val allowedVersions = List(1)

  override def process(message: DistributedMessage, request: messages.DeleteNotificationRequest, injector: ScalaInjector): JValue = {
    val notification = Notification.fetch(request.notificationIid)
    Notification.delete(notification)
    messages.DeleteNotificationResponse(request.notificationIid).toJson
  }
}

object DeleteNotificationResponse extends DistributedResponseHandler[messages.DeleteNotificationResponse] {
  override protected val responseKind = DistributedMessageKind.DeleteNotificationResponse
  override protected val allowedVersions = List(1)

  override protected def getServiceResult(response: messages.DeleteNotificationResponse, message: DistributedMessage): JValue = {
    "notificationIid" -> response.notificationIid
  }
}
