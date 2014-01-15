package com.qoid.bennu.model

trait HasInternalId {
  val iid: InternalId
  val deleted: Boolean
}
