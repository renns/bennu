package com.qoid.bennu.distributed.messages

import com.qoid.bennu.Enum
import com.qoid.bennu.EnumCompanion

sealed trait DistributedMessageKind extends Enum[DistributedMessageKind] {
  override val companion = DistributedMessageKind
}

object DistributedMessageKind extends EnumCompanion[DistributedMessageKind] {
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
