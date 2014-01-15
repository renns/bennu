package com.qoid.bennu.model

import com.qoid.bennu.JdbcAssist._

object Alias extends BennuMapperCompanion[Alias] {
}

case class Alias(
  iid: InternalId,
  rootLabelIid: InternalId,
  name: String,
  deleted: Boolean
) extends HasInternalId with BennuMappedInstance[Alias] {
  def mapper = Alias
}
