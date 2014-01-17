package com.qoid.bennu.model

import com.qoid.bennu.JdbcAssist._
import m3.jdbc.PrimaryKey
import net.liftweb.json._

object LabelAcl extends BennuMapperCompanion[LabelAcl] {
}

case class LabelAcl(
  @PrimaryKey iid: InternalId,
  connectionIid: InternalId,
  labelIid: InternalId,
  data: JValue,
  deleted: Boolean
) extends HasInternalId with BennuMappedInstance[LabelAcl] {
  def mapper = LabelAcl
}
