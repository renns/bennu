package com.qoid.bennu.distributed.messages

import com.qoid.bennu.Enum

sealed trait DistributedMessageKind

object DistributedMessageKind extends Enum[DistributedMessageKind] {
  case object QueryRequest extends DistributedMessageKind
  case object QueryResponse extends DistributedMessageKind
  case object DeRegisterStandingQuery extends DistributedMessageKind
  case object IntroductionRequest extends DistributedMessageKind
  case object IntroductionResponse extends DistributedMessageKind
  case object IntroductionConnect extends DistributedMessageKind
  case object VerificationRequest extends DistributedMessageKind
  case object VerificationResponse extends DistributedMessageKind

  override val values: Set[DistributedMessageKind] = Set(
    QueryRequest,
    QueryResponse,
    DeRegisterStandingQuery,
    IntroductionRequest,
    IntroductionResponse,
    IntroductionConnect,
    VerificationRequest,
    VerificationResponse
  )
}
