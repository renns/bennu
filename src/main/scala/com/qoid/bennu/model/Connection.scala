package com.qoid.bennu.model

import com.qoid.bennu.JdbcAssist._

object Connection extends BennuMapperCompanion[Connection] {
}

case class Connection(
  iid: InternalId,
  url: String,
  deleted: Boolean
) extends HasInternalId with BennuMappedInstance[Connection] {
  def mapper = Connection
}
