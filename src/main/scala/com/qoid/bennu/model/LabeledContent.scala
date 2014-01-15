package com.qoid.bennu.model

import m3.jdbc._
import com.qoid.bennu.JdbcAssist._

object LabeledContent extends Mapper.MapperCompanion[LabeledContent,InternalId] {
}

case class LabeledContent(
  iid: InternalId,
  content: Content,
  label: Label
)
