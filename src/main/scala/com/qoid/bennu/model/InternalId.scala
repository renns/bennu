package com.qoid.bennu.model

import m3.StringConverters.HasStringConverter
import m3.StringConverters.Converter
import m3.predef._
import net.model3.util.UidGenerator
import scala.language.implicitConversions
import m3.jdbc.RowMapper
import m3.jdbc.Row

object InternalId extends HasStringConverter {
  
  val stringConverter = new Converter[InternalId] {
    override def toString(value: InternalId) = value.value
    def fromString(value: String) = InternalId(value)
  }
  
  val uidGenerator = inject[UidGenerator]

  def random: InternalId = InternalId(uidGenerator.create(32))
  
  implicit def sqlEscape(iid: InternalId): m3.jdbc.SqlEscaped = m3.jdbc.SqlEscaped.string(iid.value)
  
  implicit lazy val rowMapper: RowMapper[InternalId] = new RowMapper[InternalId] {
    def get(i: Int, row: Row) = InternalId(row.get[String](i))
  } 
}


case class InternalId(value: String)

