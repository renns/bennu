package com.qoid.bennu.model


import m3.StringConverters.HasStringConverter
import m3.StringConverters.Converter
import m3.predef._
import net.model3.util.UidGenerator
import m3.jdbc.RowMapper
import m3.jdbc.Row
import scala.language.implicitConversions

trait AbstractIdCompanion[IdType <: AbstractId] extends HasStringConverter { outer =>
  
  val stringConverter = new Converter[IdType] {
    override def toString(value: IdType) = value.value
    def fromString(value: String) = outer.fromString(value)
  }

  def fromString(value: String): IdType
  
  val uidGenerator = inject[UidGenerator]

  def random: IdType = fromString(uidGenerator.create(32))
  
  implicit def sqlEscape(id: IdType): m3.jdbc.SqlEscaped = m3.jdbc.SqlEscaped.string(id.value)
  
  implicit lazy val rowMapper: RowMapper[InternalId] = new RowMapper[InternalId] {
    def get(i: Int, row: Row) = InternalId(row.get[String](i))
  } 
}

trait AbstractId {
  def value: String
}
