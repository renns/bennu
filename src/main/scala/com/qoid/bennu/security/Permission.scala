package com.qoid.bennu.security

import com.qoid.bennu.Enum
import com.qoid.bennu.EnumCompanion

sealed trait Permission extends Enum[Permission] {
  override val companion = Permission
}

object Permission extends EnumCompanion[Permission] {
  case object View extends Permission
  case object Insert extends Permission
  case object Update extends Permission
  case object Delete extends Permission

  override val values: Set[Permission] = Set(
    View,
    Insert,
    Update,
    Delete
  )
}
