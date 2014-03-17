package com.qoid.bennu.model.notification

import com.qoid.bennu.Enum

sealed trait NotificationKind

object NotificationKind extends Enum[NotificationKind] {
  case object IntroductionRequest extends NotificationKind

  override val values: Set[NotificationKind] = Set(
    IntroductionRequest
  )
}
