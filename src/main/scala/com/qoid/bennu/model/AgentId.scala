package com.qoid.bennu.model

import m3.StringConverters.Converter
import m3.StringConverters.HasStringConverter
import m3.predef._
import net.model3.util.UidGenerator
import scala.language.implicitConversions

object AgentId extends HasStringConverter {

  val stringConverter = new Converter[AgentId] {
    override def toString(value: AgentId) = value.value

    def fromString(value: String) = AgentId(value)
  }

  val uidGenerator = inject[UidGenerator]

  def random: AgentId = AgentId(uidGenerator.create(32))

  implicit def sqlEscape(agentId: AgentId): m3.jdbc.SqlEscaped = m3.jdbc.SqlEscaped.string(agentId.value)
}

case class AgentId(value: String) {
  def asIid = InternalId(value)
}
