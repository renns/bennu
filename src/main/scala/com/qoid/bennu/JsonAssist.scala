package com.qoid.bennu

import m3.{TypeInfo, DefaultStringConverters}
import m3.json._
import net.liftweb.json.JValue

object JsonAssist extends LiftJsonAssist {

  lazy val handlers = new Handlers.DefaultHandlerFactory(new DefaultStringConverters)
  
  implicit lazy val serializer = new DefaultJsonSerializer(handlers)

}

trait ToJsonCapable {
  def toJson: JValue = JsonAssist.serializer.toJsonTi(this, TypeInfo(this.getClass))
}

trait FromJsonCapable[T] {
  def fromJson(jv: JValue)(implicit mT: Manifest[T]): T = {
    JsonAssist.serializer.fromJson[T](jv)
  }
}
