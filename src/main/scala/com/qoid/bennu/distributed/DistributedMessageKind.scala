package com.qoid.bennu.distributed

import com.qoid.bennu.Enum
import com.qoid.bennu.EnumCompanion

sealed trait DistributedMessageKind extends Enum[DistributedMessageKind] {
  override val companion = DistributedMessageKind
  val handler: DistributedHandler
}

object DistributedMessageKind extends EnumCompanion[DistributedMessageKind] {

  // Error
  case object Error extends DistributedMessageKind { override val handler = handlers.Error }

  // Query
  case object QueryRequest extends DistributedMessageKind { override val handler = handlers.QueryRequest }
  case object QueryResponse extends DistributedMessageKind { override val handler = handlers.QueryResponse }
  case object StandingQueryResponse extends DistributedMessageKind { override val handler = handlers.StandingQueryResponse }

  // Alias
  case object CreateAliasRequest extends DistributedMessageKind { override val handler = handlers.CreateAliasRequest }
  case object CreateAliasResponse extends DistributedMessageKind { override val handler = handlers.CreateAliasResponse }
  case object UpdateAliasRequest extends DistributedMessageKind { override val handler = handlers.UpdateAliasRequest }
  case object UpdateAliasResponse extends DistributedMessageKind { override val handler = handlers.UpdateAliasResponse }
  case object DeleteAliasRequest extends DistributedMessageKind { override val handler = handlers.DeleteAliasRequest }
  case object DeleteAliasResponse extends DistributedMessageKind { override val handler = handlers.DeleteAliasResponse }
  case object CreateAliasLoginRequest extends DistributedMessageKind { override val handler = handlers.CreateAliasLoginRequest }
  case object CreateAliasLoginResponse extends DistributedMessageKind { override val handler = handlers.CreateAliasLoginResponse }
  case object UpdateAliasLoginRequest extends DistributedMessageKind { override val handler = handlers.UpdateAliasLoginRequest }
  case object UpdateAliasLoginResponse extends DistributedMessageKind { override val handler = handlers.UpdateAliasLoginResponse }
  case object DeleteAliasLoginRequest extends DistributedMessageKind { override val handler = handlers.DeleteAliasLoginRequest }
  case object DeleteAliasLoginResponse extends DistributedMessageKind { override val handler = handlers.DeleteAliasLoginResponse }
  case object UpdateAliasProfileRequest extends DistributedMessageKind { override val handler = handlers.UpdateAliasProfileRequest }
  case object UpdateAliasProfileResponse extends DistributedMessageKind { override val handler = handlers.UpdateAliasProfileResponse }

  // Connection

  // Content
  case object CreateContentRequest extends DistributedMessageKind { override val handler = messages.CreateContentRequest }
  case object CreateContentResponse extends DistributedMessageKind { override val handler = messages.CreateContentResponse }

  // Label
  case object CreateLabelRequest extends DistributedMessageKind { override val handler = messages.CreateLabelRequest }
  case object CreateLabelResponse extends DistributedMessageKind { override val handler = messages.CreateLabelResponse }

  // Notification

  // Introduction

  // Verification

  override val values: Set[DistributedMessageKind] = Set(
    Error,

    QueryRequest,
    QueryResponse,
    StandingQueryResponse,

    CreateAliasRequest,
    CreateAliasResponse,
    UpdateAliasRequest,
    UpdateAliasResponse,
    DeleteAliasRequest,
    DeleteAliasResponse,
    CreateAliasLoginRequest,
    CreateAliasLoginResponse,
    UpdateAliasLoginRequest,
    UpdateAliasLoginResponse,
    DeleteAliasLoginRequest,
    DeleteAliasLoginResponse,
    UpdateAliasProfileRequest,
    UpdateAliasProfileResponse,

    CreateContentRequest,
    CreateContentResponse,

    CreateLabelRequest,
    CreateLabelResponse
  )
}
