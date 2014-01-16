package com.qoid.bennu.model

import com.qoid.bennu.JdbcAssist._

object LabelAcl extends BennuMapperCompanion[LabelAcl] {
}

case class LabelAcl(
  iid: InternalId,
  connectionIid: InternalId,
  labelIid: InternalId,
  deleted: Boolean
) extends HasInternalId with BennuMappedInstance[LabelAcl] {
  def mapper = LabelAcl
}
