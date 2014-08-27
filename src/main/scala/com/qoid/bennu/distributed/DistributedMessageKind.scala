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
  case object CancelQueryRequest extends DistributedMessageKind { override val handler = handlers.CancelQueryRequest }

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
  case object DeleteConnectionRequest extends DistributedMessageKind { override val handler = handlers.DeleteConnectionRequest }
  case object DeleteConnectionResponse extends DistributedMessageKind { override val handler = handlers.DeleteConnectionResponse }

  // Content
  case object CreateContentRequest extends DistributedMessageKind { override val handler = handlers.CreateContentRequest }
  case object CreateContentResponse extends DistributedMessageKind { override val handler = handlers.CreateContentResponse }
  case object UpdateContentRequest extends DistributedMessageKind { override val handler = handlers.UpdateContentRequest }
  case object UpdateContentResponse extends DistributedMessageKind { override val handler = handlers.UpdateContentResponse }
  case object AddContentLabelRequest extends DistributedMessageKind { override val handler = handlers.AddContentLabelRequest }
  case object AddContentLabelResponse extends DistributedMessageKind { override val handler = handlers.AddContentLabelResponse }
  case object RemoveContentLabelRequest extends DistributedMessageKind { override val handler = handlers.RemoveContentLabelRequest }
  case object RemoveContentLabelResponse extends DistributedMessageKind { override val handler = handlers.RemoveContentLabelResponse }

  // Label
  case object CreateLabelRequest extends DistributedMessageKind { override val handler = handlers.CreateLabelRequest }
  case object CreateLabelResponse extends DistributedMessageKind { override val handler = handlers.CreateLabelResponse }
  case object UpdateLabelRequest extends DistributedMessageKind { override val handler = handlers.UpdateLabelRequest }
  case object UpdateLabelResponse extends DistributedMessageKind { override val handler = handlers.UpdateLabelResponse }
  case object MoveLabelRequest extends DistributedMessageKind { override val handler = handlers.MoveLabelRequest }
  case object MoveLabelResponse extends DistributedMessageKind { override val handler = handlers.MoveLabelResponse }
  case object CopyLabelRequest extends DistributedMessageKind { override val handler = handlers.CopyLabelRequest }
  case object CopyLabelResponse extends DistributedMessageKind { override val handler = handlers.CopyLabelResponse }
  case object RemoveLabelRequest extends DistributedMessageKind { override val handler = handlers.RemoveLabelRequest }
  case object RemoveLabelResponse extends DistributedMessageKind { override val handler = handlers.RemoveLabelResponse }
  case object GrantLabelAccessRequest extends DistributedMessageKind { override val handler = handlers.GrantLabelAccessRequest }
  case object GrantLabelAccessResponse extends DistributedMessageKind { override val handler = handlers.GrantLabelAccessResponse }
  case object RevokeLabelAccessRequest extends DistributedMessageKind { override val handler = handlers.RevokeLabelAccessRequest }
  case object RevokeLabelAccessResponse extends DistributedMessageKind { override val handler = handlers.RevokeLabelAccessResponse }
  case object UpdateLabelAccessRequest extends DistributedMessageKind { override val handler = handlers.UpdateLabelAccessRequest }
  case object UpdateLabelAccessResponse extends DistributedMessageKind { override val handler = handlers.UpdateLabelAccessResponse }

  // Notification
  case object CreateNotificationRequest extends DistributedMessageKind { override val handler = handlers.CreateNotificationRequest }
  case object CreateNotificationResponse extends DistributedMessageKind { override val handler = handlers.CreateNotificationResponse }
  case object ConsumeNotificationRequest extends DistributedMessageKind { override val handler = handlers.ConsumeNotificationRequest }
  case object ConsumeNotificationResponse extends DistributedMessageKind { override val handler = handlers.ConsumeNotificationResponse }
  case object DeleteNotificationRequest extends DistributedMessageKind { override val handler = handlers.DeleteNotificationRequest }
  case object DeleteNotificationResponse extends DistributedMessageKind { override val handler = handlers.DeleteNotificationResponse }

  // Introduction
  case object InitiateIntroductionRequest extends DistributedMessageKind { override val handler = handlers.InitiateIntroductionRequest }
  case object InitiateIntroductionResponse extends DistributedMessageKind { override val handler = handlers.InitiateIntroductionResponse }
  case object IntroductionRequest extends DistributedMessageKind { override val handler = handlers.IntroductionRequest }
  case object IntroductionResponse extends DistributedMessageKind { override val handler = handlers.IntroductionResponse }
  case object AcceptIntroductionRequest extends DistributedMessageKind { override val handler = handlers.AcceptIntroductionRequest }
  case object AcceptIntroductionResponse extends DistributedMessageKind { override val handler = handlers.AcceptIntroductionResponse }
  case object IntroductionConnect extends DistributedMessageKind { override val handler = handlers.IntroductionConnect }

  // Verification

  override val values: Set[DistributedMessageKind] = Set(
    Error,

    QueryRequest,
    QueryResponse,
    StandingQueryResponse,
    CancelQueryRequest,

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

    DeleteConnectionRequest,
    DeleteConnectionResponse,

    CreateContentRequest,
    CreateContentResponse,
    UpdateContentRequest,
    UpdateContentResponse,
    AddContentLabelRequest,
    AddContentLabelResponse,
    RemoveContentLabelRequest,
    RemoveContentLabelResponse,

    CreateLabelRequest,
    CreateLabelResponse,
    UpdateLabelRequest,
    UpdateLabelResponse,
    MoveLabelRequest,
    MoveLabelResponse,
    CopyLabelRequest,
    CopyLabelResponse,
    RemoveLabelRequest,
    RemoveLabelResponse,
    GrantLabelAccessRequest,
    GrantLabelAccessResponse,
    RevokeLabelAccessRequest,
    RevokeLabelAccessResponse,
    UpdateLabelAccessRequest,
    UpdateLabelAccessResponse,

    CreateNotificationRequest,
    CreateNotificationResponse,
    ConsumeNotificationRequest,
    ConsumeNotificationResponse,
    DeleteNotificationRequest,
    DeleteNotificationResponse,

    InitiateIntroductionRequest,
    InitiateIntroductionResponse,
    IntroductionRequest,
    IntroductionResponse,
    AcceptIntroductionRequest,
    AcceptIntroductionResponse,
    IntroductionConnect
  )
}
