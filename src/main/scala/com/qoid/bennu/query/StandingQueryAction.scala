package com.qoid.bennu.query

import com.qoid.bennu.Enum
import com.qoid.bennu.EnumCompanion

sealed trait StandingQueryAction extends Enum[StandingQueryAction] {
  override val companion = StandingQueryAction
}

object StandingQueryAction extends EnumCompanion[StandingQueryAction] {
  case object Insert extends StandingQueryAction
  case object Update extends StandingQueryAction
  case object Delete extends StandingQueryAction

  override val values: Set[StandingQueryAction] = Set(
    Insert,
    Update,
    Delete
  )

  override def valueToString(value: StandingQueryAction): String = String.valueOf(value).toLowerCase
}
