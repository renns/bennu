package com.qoid.bennu.query

import com.qoid.bennu.Enum
import com.qoid.bennu.EnumCompanion

sealed trait QueryResponseType extends Enum[QueryResponseType] {
  override val companion = QueryResponseType
}

object QueryResponseType extends EnumCompanion[QueryResponseType] {
  case object SQuery extends QueryResponseType
  case object Query extends QueryResponseType

  override val values: Set[QueryResponseType] = Set(
    SQuery,
    Query
  )

  override def valueToString(value: QueryResponseType): String = String.valueOf(value).toLowerCase
}
