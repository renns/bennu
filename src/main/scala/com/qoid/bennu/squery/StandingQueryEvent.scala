package com.qoid.bennu.squery

import com.qoid.bennu.ToJsonCapable
import net.liftweb.json.JValue

case class StandingQueryEvent(
  action: StandingQueryAction,
  `type`: String,
  instance: JValue
) extends ToJsonCapable
