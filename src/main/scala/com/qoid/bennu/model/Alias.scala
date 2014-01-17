package com.qoid.bennu.model

import com.qoid.bennu.JdbcAssist._
import m3.jdbc.PrimaryKey
import net.liftweb.json._

object Alias extends BennuMapperCompanion[Alias] {
}

case class Alias(
  @PrimaryKey iid: InternalId,
  rootLabelIid: InternalId,
  name: String,
  data: JValue,
  deleted: Boolean
) extends HasInternalId with BennuMappedInstance[Alias] {
  def mapper = Alias
}
