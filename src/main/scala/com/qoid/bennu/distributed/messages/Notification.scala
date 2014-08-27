package com.qoid.bennu.distributed.messages

import com.qoid.bennu.JsonAssist._
import com.qoid.bennu.ToJsonCapable
import com.qoid.bennu.model.Notification
import com.qoid.bennu.model.id.InternalId

case class CreateNotificationRequest(kind: String, data: JValue) extends ToJsonCapable
case class CreateNotificationResponse(notification: Notification) extends ToJsonCapable

case class ConsumeNotificationRequest(notificationIid: InternalId) extends ToJsonCapable
case class ConsumeNotificationResponse(notificationIid: InternalId) extends ToJsonCapable

case class DeleteNotificationRequest(notificationIid: InternalId) extends ToJsonCapable
case class DeleteNotificationResponse(notificationIid: InternalId) extends ToJsonCapable
