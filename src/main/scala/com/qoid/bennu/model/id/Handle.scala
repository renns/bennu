package com.qoid.bennu.model.id

import scala.language.implicitConversions

object Handle extends AbstractIdCompanion[Handle] {
  def fromString(value: String) = Handle(value)
}

case class Handle(value: String) extends AbstractId

