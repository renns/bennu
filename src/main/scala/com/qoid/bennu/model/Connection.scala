package com.qoid.bennu.model

import m3.jdbc._
import com.qoid.bennu.JdbcAssist._

object Connection extends BennuMapperCompanion[Connection] {
}

case class Connection(
  iid: InternalId,
  url: String
) extends HasInternalId with BennuMappedInstance[Connection] {
  def mapper = Connection
}
