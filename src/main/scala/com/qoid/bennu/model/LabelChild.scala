package com.qoid.bennu.model

import com.qoid.bennu.JdbcAssist._
import m3.jdbc.PrimaryKey

object LabelChild extends BennuMapperCompanion[LabelChild] {
}

case class LabelChild(
  @PrimaryKey iid: InternalId,
  parentIid: InternalId,
  childIid: InternalId,
  deleted: Boolean
) extends HasInternalId with BennuMappedInstance[LabelChild] {
  def mapper = LabelChild
}
