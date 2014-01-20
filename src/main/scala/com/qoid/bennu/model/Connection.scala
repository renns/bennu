package com.qoid.bennu.model

import com.qoid.bennu.JdbcAssist._
import m3.jdbc.PrimaryKey
import net.liftweb.json._

object Connection extends BennuMapperCompanion[Connection] {
}

case class Connection(
  @PrimaryKey iid: InternalId,
  url: String,
  data: JValue,
  deleted: Boolean = false
) extends HasInternalId with BennuMappedInstance[Connection] {
  def mapper = Connection
}
