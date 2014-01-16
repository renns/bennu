package com.qoid.bennu.model

import com.qoid.bennu.JdbcAssist._
import m3.jdbc.PrimaryKey

object Connection extends BennuMapperCompanion[Connection] {
}

case class Connection(
  @PrimaryKey iid: InternalId,
  url: String,
  deleted: Boolean
) extends HasInternalId with BennuMappedInstance[Connection] {
  def mapper = Connection
}
