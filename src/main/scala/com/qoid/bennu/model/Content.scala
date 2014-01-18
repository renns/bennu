package com.qoid.bennu.model

import com.qoid.bennu.JdbcAssist._
import m3.jdbc.PrimaryKey

object Content extends BennuMapperCompanion[Content] {
}

case class Content(
  @PrimaryKey iid: InternalId,
  contentType: String,
  blob: String,
  deleted: Boolean = false
) extends HasInternalId with BennuMappedInstance[Content] {
  def mapper = Content
}

