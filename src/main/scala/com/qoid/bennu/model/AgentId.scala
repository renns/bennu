package com.qoid.bennu.model

import com.qoid.bennu.JsonAssist._
import m3.json.Handlers.HasJsonHandler
import m3.json.Handlers.SimpleTypeHandler
import m3.json.Serialization.JsonWriter
import m3.json.Serialization.TypeHandler
import scala.language.implicitConversions

object AgentId extends AbstractIdCompanion[AgentId] with HasJsonHandler {
  def fromString(value: String) = AgentId(value)

  override def jsonHandler: TypeHandler[AgentId] = {
    new SimpleTypeHandler[AgentId] {
      override def partialRead: PartialFunction[JValue, AgentId] = {
        case _ => AgentId("")
      }

      override def write(value: AgentId, writer: JsonWriter): JValue = JNothing
    }
  }
}

case class AgentId(value: String) extends AbstractId {
  def asIid = InternalId(value)
}
