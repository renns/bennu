package com.qoid.bennu.model.introduction

import com.qoid.bennu.Enum
import com.qoid.bennu.EnumCompanion

sealed trait IntroductionState extends Enum[IntroductionState] {
  override val companion = IntroductionState
}

object IntroductionState extends EnumCompanion[IntroductionState] {
  case object NotResponded extends IntroductionState
  case object Accepted extends IntroductionState
  case object Rejected extends IntroductionState

  override val values: Set[IntroductionState] = Set(
    NotResponded,
    Accepted,
    Rejected
  )
}
