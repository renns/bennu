package com.qoid.bennu.model

import m3.jdbc._
import com.qoid.bennu.JdbcAssist._

object Alias extends Mapper.MapperCompanion[Alias,InternalId] {
}

case class Alias(
  iid: InternalId,
  rootLabelIid: InternalId,
  name: String
) extends HasInternalId
