package com.qoid.bennu.model

import m3.jdbc._
import com.qoid.bennu.JdbcAssist._

object LabelChild extends Mapper.MapperCompanion[LabelChild,InternalId] {
}

case class LabelChild(
  iid: InternalId,
  parent: Label, //How are relationships done in this model?
  child: Label
)
