package com.qoid.bennu.distributed.messages

import com.qoid.bennu.Enum

sealed trait DistributedMessageKind

object DistributedMessageKind extends Enum[DistributedMessageKind] {
  case object QueryRequest extends DistributedMessageKind
  case object QueryResponse extends DistributedMessageKind
  case object NotSupported extends DistributedMessageKind
  //Need to be able to de-serialize anything else into a default value

  override val values: Set[DistributedMessageKind] = Set(
    QueryRequest,
    QueryResponse,
    NotSupported
  )
}
