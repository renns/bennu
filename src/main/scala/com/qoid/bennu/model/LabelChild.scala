package com.qoid.bennu.model

import com.qoid.bennu.JdbcAssist._
import m3.jdbc.PrimaryKey
import net.liftweb.json._

object LabelChild extends BennuMapperCompanion[LabelChild] {
}

case class LabelChild(
  @PrimaryKey iid: InternalId,
  agentId: AgentId,
  parentIid: InternalId,
  childIid: InternalId,
  data: JValue,
  deleted: Boolean = false
) extends HasInternalId[LabelChild] with BennuMappedInstance[LabelChild] {
  def mapper = LabelChild
}
