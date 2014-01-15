package com.qoid.bennu.model

import m3.jdbc._
import com.qoid.bennu.JdbcAssist._

object LabelACL extends Mapper.MapperCompanion[LabelACL,InternalId] {
}

case class LabelACL(
  iid: InternalId,
  connectionIid: InternalId,
  labelIid: InternalId
) extends HasInternalId
