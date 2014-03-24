package com.qoid.bennu.model.notification

import com.qoid.bennu.Enum

sealed trait NotificationKind

object NotificationKind extends Enum[NotificationKind] {
  case object IntroductionRequest extends NotificationKind
  case object VerificationRequest extends NotificationKind
  case object VerificationResponse extends NotificationKind

  override val values: Set[NotificationKind] = Set(
    IntroductionRequest,
    VerificationRequest,
    VerificationResponse
  )
}
