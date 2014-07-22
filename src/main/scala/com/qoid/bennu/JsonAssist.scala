package com.qoid.bennu

import m3.json._
import m3.DefaultStringConverters
import m3.TypeInfo
import net.liftweb.json.JValue

object JsonAssist extends LiftJsonAssist {

  lazy val handlers = new Handlers.DefaultHandlerFactory(new DefaultStringConverters)
  
  implicit lazy val serializer = new DefaultJsonSerializer(handlers)

  def toJson(a: Any): JValue = serializer.toJsonTi(a, TypeInfo(a.getClass))
}

trait ToJsonCapable {
  def toJson: JValue = JsonAssist.serializer.toJsonTi(this, TypeInfo(this.getClass))
}

trait FromJsonCapable[T] {
  def fromJson(jv: JValue)(implicit mT: Manifest[T]): T = {
    JsonAssist.serializer.fromJson[T](jv)
  }
}
