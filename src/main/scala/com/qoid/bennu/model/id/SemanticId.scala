package com.qoid.bennu.model.id

object SemanticId extends AbstractIdCompanion[SemanticId] {
  def fromString(s: String) = SemanticId(s)
}

case class SemanticId(value: String) extends AbstractId
