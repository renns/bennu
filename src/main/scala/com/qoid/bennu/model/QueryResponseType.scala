package com.qoid.bennu.model

import com.qoid.bennu.Enum

sealed trait QueryResponseType

object QueryResponseType extends Enum[QueryResponseType] {
  case object SQuery extends QueryResponseType
  case object Query extends QueryResponseType

  override val values: Set[QueryResponseType] = Set(
    SQuery,
    Query
  )

  override def valueToString(value: QueryResponseType): String = String.valueOf(value).toLowerCase
}
