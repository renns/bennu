package com.qoid.bennu.model

import com.qoid.bennu.JdbcAssist._
import m3.jdbc.PrimaryKey

object Alias extends BennuMapperCompanion[Alias] {
}

case class Alias(
  @PrimaryKey iid: InternalId,
  rootLabelIid: InternalId,
  name: String,
  deleted: Boolean = false
) extends HasInternalId with BennuMappedInstance[Alias] {
  def mapper = Alias
}
