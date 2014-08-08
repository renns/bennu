package com.qoid.bennu.distributed.messages

import com.qoid.bennu.ToJsonCapable
import com.qoid.bennu.model.id.InternalId

case class ConsumeNotificationRequest(notificationIid: InternalId) extends ToJsonCapable
case class ConsumeNotificationResponse(notificationIid: InternalId) extends ToJsonCapable
