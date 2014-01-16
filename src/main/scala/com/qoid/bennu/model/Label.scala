package com.qoid.bennu.model

import com.qoid.bennu.JdbcAssist._
import m3.jdbc.PrimaryKey

object Label extends BennuMapperCompanion[Label] {
}

case class Label(
  @PrimaryKey iid: InternalId,
  name: String,
  deleted: Boolean
) extends HasInternalId with BennuMappedInstance[Label] {
  def mapper = Label
}
