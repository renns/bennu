package com.qoid.bennu.squery

import com.qoid.bennu.ToJsonCapable
import m3.json.Json
import net.liftweb.json.JValue

case class StandingQueryEvent(
  action: StandingQueryAction,
  @Json("type") tpe: String,
  instance: JValue
) extends ToJsonCapable
