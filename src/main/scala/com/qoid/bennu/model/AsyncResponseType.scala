package com.qoid.bennu.model

import com.qoid.bennu.Enum

sealed trait AsyncResponseType

object AsyncResponseType extends Enum[AsyncResponseType] {
  case object SQuery extends AsyncResponseType
  case object SQuery2 extends AsyncResponseType
  case object Profile extends AsyncResponseType
  case object Query extends AsyncResponseType

  override val values: Set[AsyncResponseType] = Set(
    SQuery,
    SQuery2,
    Profile,
    Query
  )

  override def valueToString(value: AsyncResponseType): String = String.valueOf(value).toLowerCase
}
