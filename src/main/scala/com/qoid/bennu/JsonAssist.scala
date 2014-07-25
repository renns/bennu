package com.qoid.bennu

import m3.DefaultStringConverters
import m3.TypeInfo
import m3.json.LiftJsonAssist._
import m3.json._

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
