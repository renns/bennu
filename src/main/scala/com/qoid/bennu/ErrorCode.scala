package com.qoid.bennu

sealed trait ErrorCode extends Enum[ErrorCode] {
  override val companion = ErrorCode
}

object ErrorCode extends EnumCompanion[ErrorCode] {
  case object SecurityValidationFailed extends ErrorCode
  case object Forbidden extends ErrorCode
  case object Timeout extends ErrorCode
  case object AgentNameExists extends ErrorCode
  case object ImportAgentFailure extends ErrorCode
  case object Generic extends ErrorCode

  override val values: Set[ErrorCode] = Set(
    SecurityValidationFailed,
    Forbidden,
    Timeout,
    AgentNameExists,
    ImportAgentFailure,
    Generic
  )
}
