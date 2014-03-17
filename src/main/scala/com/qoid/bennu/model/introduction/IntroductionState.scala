package com.qoid.bennu.model.introduction

import com.qoid.bennu.Enum

sealed trait IntroductionState

object IntroductionState extends Enum[IntroductionState] {
  case object NotResponded extends IntroductionState
  case object Accepted extends IntroductionState
  case object Rejected extends IntroductionState

  override val values: Set[IntroductionState] = Set(
    NotResponded,
    Accepted,
    Rejected
  )
}
