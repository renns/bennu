package com.qoid.bennu.model

import m3.jdbc._
import com.qoid.bennu.JdbcAssist._

object LabelChild extends BennuMapperCompanion[LabelChild] {
}

case class LabelChild(
  iid: InternalId,
  parentIid: InternalId,
  childIid: InternalId
) extends HasInternalId with BennuMappedInstance[LabelChild] {
  def mapper = LabelChild
}
