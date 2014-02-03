package com.qoid.bennu.model

import com.qoid.bennu.JsonCapable
import net.liftweb.json.JValue
import m3.jdbc.ColumnMapper
import m3.jdbc.Mapper
import com.qoid.bennu.JdbcAssist.BennuMapperCompanion


trait HasInternalId extends JsonCapable {
  val iid: InternalId
  val agentId: AgentId
  val data: JValue
  val deleted: Boolean
  def mapper: BennuMapperCompanion[_]
}
