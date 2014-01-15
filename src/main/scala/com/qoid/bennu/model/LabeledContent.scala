package com.qoid.bennu.model

import com.qoid.bennu.JdbcAssist._

object LabeledContent extends BennuMapperCompanion[LabeledContent] {
}

case class LabeledContent(
  iid: InternalId,
  contentIid: InternalId,
  labelIid: InternalId,
  deleted: Boolean
) extends HasInternalId with BennuMappedInstance[LabeledContent] {
  def mapper = LabeledContent
}
