package com.qoid.bennu.model

import com.qoid.bennu.JdbcAssist._
import m3.jdbc.PrimaryKey
import net.liftweb.json._

object Content extends BennuMapperCompanion[Content] {
}

case class Content(
  @PrimaryKey iid: InternalId,
  contentType: String,
  blob: String,
  data: JValue,
  deleted: Boolean
) extends HasInternalId with BennuMappedInstance[Content] {
  def mapper = Content
}

