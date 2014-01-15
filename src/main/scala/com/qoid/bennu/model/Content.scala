package com.qoid.bennu.model

import com.qoid.bennu.JdbcAssist._

object Content extends BennuMapperCompanion[Content] {
}

case class Content(
  iid: InternalId,
  contentType: String,
  blob: String,
  deleted: Boolean
) extends HasInternalId with BennuMappedInstance[Content] {
  def mapper = Content
}

