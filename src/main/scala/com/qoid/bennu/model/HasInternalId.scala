package com.qoid.bennu.model

import com.qoid.bennu.JsonCapable
import net.liftweb.json.JValue

trait HasInternalId extends JsonCapable {
  val iid: InternalId
  val agentId: AgentId
  val data: JValue
  val deleted: Boolean
}
