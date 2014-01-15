package com.qoid.bennu.model

import m3.jdbc._
import com.qoid.bennu.JdbcAssist._

object Alias extends BennuMapperCompanion[Alias] {
}

case class Alias(
  iid: InternalId,
  rootLabelIid: InternalId,
  name: String
) extends HasInternalId with BennuMappedInstance[Alias] {
  def mapper = Alias
}
