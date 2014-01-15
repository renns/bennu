package com.qoid.bennu.model


import m3.predef._
import m3.jdbc._
import com.qoid.bennu.JdbcAssist._

object Label extends Mapper.MapperCompanion[Label,InternalId] {

//  def fetchByName(name: String)(implicit conn: Connection) = {
//    selectBox(sql"""name = ${name}""")
//  }
  
}


case class Label(
  iid: InternalId,
  name: String
) extends HasInternalId

