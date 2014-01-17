package com.qoid.bennu.model

import com.qoid.bennu.JdbcAssist._
import m3.jdbc.PrimaryKey
import net.liftweb.json._

object Label extends BennuMapperCompanion[Label] {
}

case class Label(
  @PrimaryKey iid: InternalId,
  name: String,
  data: JValue,
  deleted: Boolean
) extends HasInternalId with BennuMappedInstance[Label] {
  def mapper = Label
}
