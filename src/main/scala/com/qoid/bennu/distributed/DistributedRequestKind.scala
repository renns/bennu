package com.qoid.bennu.distributed

import com.qoid.bennu.Enum

sealed trait DistributedRequestKind

object DistributedRequestKind extends Enum[DistributedRequestKind] {
  
  case object GetProfile extends DistributedRequestKind
  case object Notification extends DistributedRequestKind
  case object Query extends DistributedRequestKind

  override val values: Set[DistributedRequestKind] = Set(
    GetProfile,
    Notification,
    Query
  )
}
