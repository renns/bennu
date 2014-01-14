package com.qoid.bennu

import m3.json.LiftJsonAssist
import m3.json.Serialization

object JsonAssist extends LiftJsonAssist {
  implicit val serializer = Serialization.simpleJsonSerializer
}