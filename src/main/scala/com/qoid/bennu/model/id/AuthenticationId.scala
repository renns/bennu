package com.qoid.bennu.model.id

object AuthenticationId extends AbstractIdCompanion[AuthenticationId] {
  def fromString(s: String) = AuthenticationId(s)
}

case class AuthenticationId(value: String) extends AbstractId
