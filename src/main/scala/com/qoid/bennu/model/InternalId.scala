package com.qoid.bennu.model

import m3.StringConverters.HasStringConverter
import m3.StringConverters.Converter
import m3.predef._
import net.model3.util.UidGenerator
import scala.language.implicitConversions
import m3.jdbc.RowMapper
import m3.jdbc.Row

object InternalId extends AbstractIdCompanion[InternalId] {
  def fromString(value: String) = InternalId(value)
}


case class InternalId(value: String) extends AbstractId

