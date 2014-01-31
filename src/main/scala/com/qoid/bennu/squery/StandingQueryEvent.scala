package com.qoid.bennu.squery

import com.qoid.bennu.JsonCapable
import com.qoid.bennu.model.InternalId
import net.liftweb.json.JValue

case class StandingQueryEvent(
  action: StandingQueryAction,
  handle: InternalId,
  `type`: String,
  instance: JValue
) extends JsonCapable
