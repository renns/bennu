package com.qoid.bennu.model.id

import scala.language.implicitConversions

object DistributedMessageId extends AbstractIdCompanion[DistributedMessageId] {
  def fromString(value: String) = DistributedMessageId(value)
}

case class DistributedMessageId(value: String) extends AbstractId
