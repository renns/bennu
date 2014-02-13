package com.qoid.bennu.distributed

import com.qoid.bennu.FromJsonCapable
import com.qoid.bennu.JsonAssist._
import com.qoid.bennu.ToJsonCapable
import com.qoid.bennu.model.NotificationKind

object NotificationRequest extends FromJsonCapable[NotificationRequest]

case class NotificationRequest(
  kind: NotificationKind,
  data: JValue
) extends ToJsonCapable
