package com.qoid.bennu.model

import m3.jdbc._
import com.qoid.bennu.JdbcAssist._

object LabelACL extends BennuMapperCompanion[LabelACL] {
}

@SqlName("label_acl")
case class LabelACL(
  iid: InternalId,
  connectionIid: InternalId,
  labelIid: InternalId
) extends HasInternalId with BennuMappedInstance[LabelACL] {
  def mapper = LabelACL
}
