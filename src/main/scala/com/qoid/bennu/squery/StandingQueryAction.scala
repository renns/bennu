package com.qoid.bennu.squery

import com.qoid.bennu.Enum

sealed trait StandingQueryAction

object StandingQueryAction extends Enum[StandingQueryAction] {
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
