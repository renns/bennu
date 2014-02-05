package com.qoid.bennu

sealed trait ErrorCode

object ErrorCode extends Enum[ErrorCode] {
  case object SecurityValidationFailed extends ErrorCode

  override val values: Set[ErrorCode] = Set(
    SecurityValidationFailed
  )
}
