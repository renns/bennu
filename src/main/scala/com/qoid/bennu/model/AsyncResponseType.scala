package com.qoid.bennu.model

import com.qoid.bennu.Enum

sealed trait AsyncResponseType

object AsyncResponseType extends Enum[AsyncResponseType] {
  case object SQuery extends AsyncResponseType
  case object Profile extends AsyncResponseType

  override val values: Set[AsyncResponseType] = Set(
    SQuery,
    Profile
  )

  override def valueToString(value: AsyncResponseType): String = String.valueOf(value).toLowerCase
}