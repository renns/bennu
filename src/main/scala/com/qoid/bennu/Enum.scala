package com.qoid.bennu

import m3.StringConverters.Converter
import m3.StringConverters.HasStringConverter
import m3.predef._

trait Enum[A] {
  val companion: EnumCompanion[A]
}

trait EnumCompanion[A] extends HasStringConverter {
  val values: Set[A]

  override val stringConverter = new Converter[A] {
    override def toString(value: A): String = valueToString(value)
    override def fromString(value: String): A = {
      values.find(String.valueOf(_) =:= value) match {
        case Some(v) => v
        case _ => throw new RuntimeException(s"unable to convert '${value}'")
      }
    }
  }

  def valueToString(value: A): String = String.valueOf(value)
}
