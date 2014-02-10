package com.qoid.bennu.model

import m3.StringConverters.Converter
import m3.StringConverters.HasStringConverter
import m3.predef._
import net.model3.util.UidGenerator
import scala.language.implicitConversions

object AgentId extends AbstractIdCompanion[AgentId] {

  def fromString(value: String) = AgentId(value)

}

case class AgentId(value: String) extends AbstractId {
  def asIid = InternalId(value)
}
