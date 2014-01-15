package com.qoid.bennu.model

import m3.jdbc._
import com.qoid.bennu.JdbcAssist._

object Connection extends Mapper.MapperCompanion[Connection,InternalId] {
}

case class Connection(
  iid: InternalId,
  url: String //Should url be a URI?
)
