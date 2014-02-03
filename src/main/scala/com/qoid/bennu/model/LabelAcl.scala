package com.qoid.bennu.model

import com.qoid.bennu.JdbcAssist._
import m3.jdbc.PrimaryKey
import net.liftweb.json._

object LabelAcl extends BennuMapperCompanion[LabelAcl] {
}

case class LabelAcl(
  @PrimaryKey iid: InternalId,
  agentId: AgentId,
  connectionIid: InternalId,
  labelIid: InternalId,
  data: JValue,
  deleted: Boolean = false
) extends HasInternalId[LabelAcl] with BennuMappedInstance[LabelAcl] {
  def mapper = LabelAcl
}
