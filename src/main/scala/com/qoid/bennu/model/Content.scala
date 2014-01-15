package com.qoid.bennu.model

import m3.jdbc._
import com.qoid.bennu.JdbcAssist._

object Content extends Mapper.MapperCompanion[Content,InternalId] {
}

case class Content(
  iid: InternalId,
  contentType: String,
  blob: String
) extends HasInternalId
