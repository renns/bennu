package com.qoid.bennu.model

import com.qoid.bennu.JdbcAssist._
import m3.jdbc.PrimaryKey
import net.liftweb.json._

object LabeledContent extends BennuMapperCompanion[LabeledContent] {
}

case class LabeledContent(
  @PrimaryKey iid: InternalId,
  contentIid: InternalId,
  labelIid: InternalId,
  data: JValue,
  deleted: Boolean
) extends HasInternalId with BennuMappedInstance[LabeledContent] {
  def mapper = LabeledContent
}
