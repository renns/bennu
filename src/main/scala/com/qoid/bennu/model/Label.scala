package com.qoid.bennu.model


import m3.predef._
import m3.jdbc._
import com.qoid.bennu.JdbcAssist._

object Label extends BennuMapperCompanion[Label] {

}


case class Label(
  iid: InternalId,
  name: String
) extends HasInternalId with BennuMappedInstance[Label] {
  def mapper = Label
}

