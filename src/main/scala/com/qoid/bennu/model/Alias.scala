package com.qoid.bennu.model

import com.qoid.bennu.JdbcAssist._
import m3.jdbc.PrimaryKey
import net.liftweb.json._

object Alias extends BennuMapperCompanion[Alias] {
}

case class Alias(
  @PrimaryKey iid: InternalId,
  agentId: AgentId,
  rootLabelIid: InternalId,
  name: String,
  deleted: Boolean = false,
  data: JValue
) extends HasInternalId[Alias] with BennuMappedInstance[Alias] {
  def mapper = Alias
}
