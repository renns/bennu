package com.qoid.bennu.model

import com.qoid.bennu.JdbcAssist._
import m3.jdbc.PrimaryKey

object LabeledContent extends BennuMapperCompanion[LabeledContent] {
}

case class LabeledContent(
  @PrimaryKey iid: InternalId,
  contentIid: InternalId,
  labelIid: InternalId,
  deleted: Boolean = false
) extends HasInternalId with BennuMappedInstance[LabeledContent] {
  def mapper = LabeledContent
}
