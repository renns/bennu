package com.qoid.bennu

import m3.json.LiftJsonAssist
import m3.json.Serialization
import m3.json.DefaultJsonSerializer
import m3.json.Handlers
import m3.DefaultStringConverters

object JsonAssist extends LiftJsonAssist {

  lazy val handlers = new Handlers.DefaultHandlerFactory(new DefaultStringConverters)
  
  implicit lazy val serializer = new DefaultJsonSerializer(handlers)

}