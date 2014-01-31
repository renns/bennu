package com.qoid.bennu

import m3.DefaultStringConverters
import m3.TypeInfo
import m3.json._
import net.liftweb.json.JValue

object JsonAssist extends LiftJsonAssist {

  lazy val handlers = new Handlers.DefaultHandlerFactory(new DefaultStringConverters)
  
  implicit lazy val serializer = new DefaultJsonSerializer(handlers)

}

trait JsonCapable {
  def toJson: JValue = JsonAssist.serializer.toJsonTi(this, TypeInfo(this.getClass))
}
