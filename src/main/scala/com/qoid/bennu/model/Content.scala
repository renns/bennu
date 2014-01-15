package com.qoid.bennu.model

import m3.jdbc._
import com.qoid.bennu.JdbcAssist._

object Content extends BennuMapperCompanion[Content] {
}

case class Content(
  iid: InternalId,
  contentType: String,
  blob: String
) extends HasInternalId with BennuMappedInstance[Content] {
  def mapper = Content
}

