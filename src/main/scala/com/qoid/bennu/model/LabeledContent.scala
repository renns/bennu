package com.qoid.bennu.model

import m3.jdbc._
import com.qoid.bennu.JdbcAssist._

object LabeledContent extends BennuMapperCompanion[LabeledContent] {
}

case class LabeledContent(
  iid: InternalId,
  contentIid: InternalId,
  labelIid: InternalId
) extends HasInternalId with BennuMappedInstance[LabeledContent] {
  def mapper = LabeledContent
}
