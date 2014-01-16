package com.qoid.bennu.model

import m3.predef._
import net.liftweb.json.JValue
import com.qoid.bennu.JsonAssist
import m3.TypeInfo

trait HasInternalId {
  val iid: InternalId
  val deleted: Boolean
  def toJson: JValue = JsonAssist.serializer.toJsonTi(this, TypeInfo(this.getClass))  
}
