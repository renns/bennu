package com.qoid.bennu.model

import com.qoid.bennu.JdbcAssist._

object Label extends BennuMapperCompanion[Label] {
}

case class Label(
  iid: InternalId,
  name: String,
  deleted: Boolean
) extends HasInternalId with BennuMappedInstance[Label] {
  def mapper = Label
}
