package com.qoid.bennu.model

import com.qoid.bennu.JdbcAssist._

object LabelChild extends BennuMapperCompanion[LabelChild] {
}

case class LabelChild(
  iid: InternalId,
  parentIid: InternalId,
  childIid: InternalId,
  deleted: Boolean
) extends HasInternalId with BennuMappedInstance[LabelChild] {
  def mapper = LabelChild
}
