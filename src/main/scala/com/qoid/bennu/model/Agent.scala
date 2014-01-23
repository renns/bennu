package com.qoid.bennu.model

import com.qoid.bennu.JdbcAssist._
import m3.jdbc.PrimaryKey
import net.liftweb.json._

object Agent extends BennuMapperCompanion[Agent] {
}

case class Agent(
  @PrimaryKey iid: InternalId,
  name: String,
  data: JValue,
  deleted: Boolean = false
) extends HasInternalId with BennuMappedInstance[Agent] {
  lazy val agentId: AgentId = AgentId(iid.value)
  def mapper = Agent
}
