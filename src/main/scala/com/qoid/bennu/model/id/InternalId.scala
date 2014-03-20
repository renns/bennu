package com.qoid.bennu.model.id

import scala.language.implicitConversions

object InternalId extends AbstractIdCompanion[InternalId] {
  def fromString(value: String) = InternalId(value)
}


case class InternalId(value: String) extends AbstractId

