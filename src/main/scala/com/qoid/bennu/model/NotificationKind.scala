package com.qoid.bennu.model

import com.qoid.bennu.Enum

sealed trait NotificationKind

object NotificationKind extends Enum[NotificationKind] {
  case object Ping extends NotificationKind
  case object Pong extends NotificationKind
  case object IntroductionRequest extends NotificationKind
  case object IntroductionResponse extends NotificationKind

  override val values: Set[NotificationKind] = Set(
    Ping,
    Pong,
    IntroductionRequest,
    IntroductionResponse
  )
}
