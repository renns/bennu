package com.qoid.bennu.distributed

import com.qoid.bennu.Enum
import com.qoid.bennu.EnumCompanion
import m3.predef._

sealed trait DistributedMessageKind extends Enum[DistributedMessageKind] {
  override val companion = DistributedMessageKind
  val handle: (DistributedMessage, ScalaInjector) => Unit = (_, _) => () //TODO: Remove ' = (_, _) => ()'
}

object DistributedMessageKind extends EnumCompanion[DistributedMessageKind] {

  case object Error extends DistributedMessageKind {
    override val handle: (DistributedMessage, ScalaInjector) => Unit = messages.Error.handle
  }

  case object QueryRequest extends DistributedMessageKind {
    override val handle: (DistributedMessage, ScalaInjector) => Unit = messages.QueryRequest.handle
  }

  case object QueryResponse extends DistributedMessageKind {
    override val handle: (DistributedMessage, ScalaInjector) => Unit = messages.QueryResponse.handle
  }

  case object StandingQueryResponse extends DistributedMessageKind {
    override val handle: (DistributedMessage, ScalaInjector) => Unit = messages.StandingQueryResponse.handle
  }

  case object CreateContentRequest extends DistributedMessageKind {
    override val handle: (DistributedMessage, ScalaInjector) => Unit = messages.CreateContentRequest.handle
  }

  case object CreateContentResponse extends DistributedMessageKind {
    override val handle: (DistributedMessage, ScalaInjector) => Unit = messages.CreateContentResponse.handle
  }

  case object CreateLabelRequest extends DistributedMessageKind {
    override val handle: (DistributedMessage, ScalaInjector) => Unit = messages.CreateLabelRequest.handle
  }

  case object CreateLabelResponse extends DistributedMessageKind {
    override val handle: (DistributedMessage, ScalaInjector) => Unit = messages.CreateLabelResponse.handle
  }

  case object IntroductionRequest extends DistributedMessageKind
  case object IntroductionResponse extends DistributedMessageKind
  case object IntroductionConnect extends DistributedMessageKind
  case object VerificationRequest extends DistributedMessageKind
  case object VerificationResponse extends DistributedMessageKind

  override val values: Set[DistributedMessageKind] = Set(
    Error,
    QueryRequest,
    QueryResponse,
    StandingQueryResponse,
    CreateContentRequest,
    CreateContentResponse,
    CreateLabelRequest,
    CreateLabelResponse,
    IntroductionRequest,
    IntroductionResponse,
    IntroductionConnect,
    VerificationRequest,
    VerificationResponse
  )
}
