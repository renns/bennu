package com.qoid.bennu.model

import com.qoid.bennu.JdbcAssist._
import m3.jdbc.PrimaryKey

object LabelAcl extends BennuMapperCompanion[LabelAcl] {
}

case class LabelAcl(
  @PrimaryKey iid: InternalId,
  connectionIid: InternalId,
  labelIid: InternalId,
  deleted: Boolean
) extends HasInternalId with BennuMappedInstance[LabelAcl] {
  def mapper = LabelAcl
}
