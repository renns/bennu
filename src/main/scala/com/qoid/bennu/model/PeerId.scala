package com.qoid.bennu.model


object PeerId extends AbstractIdCompanion[PeerId] {
  
  def fromString(s: String) = PeerId(s)

}

case class PeerId(value: String) extends AbstractId

