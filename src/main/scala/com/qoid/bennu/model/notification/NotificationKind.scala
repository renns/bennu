package com.qoid.bennu.model.notification

import com.qoid.bennu.Enum
import com.qoid.bennu.EnumCompanion

sealed trait NotificationKind extends Enum[NotificationKind] {
  override val companion = NotificationKind
}

object NotificationKind extends EnumCompanion[NotificationKind] {
  case object IntroductionRequest extends NotificationKind
  case object VerificationRequest extends NotificationKind
  case object VerificationResponse extends NotificationKind

  override val values: Set[NotificationKind] = Set(
    IntroductionRequest,
    VerificationRequest,
    VerificationResponse
  )
}
