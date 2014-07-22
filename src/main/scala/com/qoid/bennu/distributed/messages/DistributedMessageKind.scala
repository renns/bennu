package com.qoid.bennu.distributed.messages

import com.qoid.bennu.Enum
import com.qoid.bennu.EnumCompanion
import com.qoid.bennu.distributed.messages
import m3.predef._

sealed trait DistributedMessageKind extends Enum[DistributedMessageKind] {
  override val companion = DistributedMessageKind
  val handle: (DistributedMessage, ScalaInjector) => Unit = (_, _) => () //TODO: Remove ' = (_, _) => ()'
}

object DistributedMessageKind extends EnumCompanion[DistributedMessageKind] {

  case object QueryRequest extends DistributedMessageKind {
    override val handle: (DistributedMessage, ScalaInjector) => Unit = messages.QueryRequest.handle
  }

  case object QueryResponse extends DistributedMessageKind {
    override val handle: (DistributedMessage, ScalaInjector) => Unit = messages.QueryResponse.handle
  }

  case object StandingQueryResponse extends DistributedMessageKind {
    override val handle: (DistributedMessage, ScalaInjector) => Unit = messages.StandingQueryResponse.handle
  }

  case object IntroductionRequest extends DistributedMessageKind
  case object IntroductionResponse extends DistributedMessageKind
  case object IntroductionConnect extends DistributedMessageKind
  case object VerificationRequest extends DistributedMessageKind
  case object VerificationResponse extends DistributedMessageKind

  override val values: Set[DistributedMessageKind] = Set(
    QueryRequest,
    QueryResponse,
    StandingQueryResponse,
    IntroductionRequest,
    IntroductionResponse,
    IntroductionConnect,
    VerificationRequest,
    VerificationResponse
  )
}
