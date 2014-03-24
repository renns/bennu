package com.qoid.bennu.model.id

object SharedId extends AbstractIdCompanion[SharedId] {
  def fromString(s: String) = SharedId(s)
}

case class SharedId(value: String) extends AbstractId
