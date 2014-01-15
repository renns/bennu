package com.qoid.bennu.model

import m3.StringConverters.HasStringConverter
import m3.StringConverters.Converter
import m3.predef._
import net.model3.util.UidGenerator

object InternalId extends HasStringConverter {
  
  val stringConverter = new Converter[InternalId] {
    override def toString(value: InternalId) = value.value
    def fromString(value: String) = InternalId(value)
  }
  
  val uidGenerator = inject[UidGenerator]

  def apply: InternalId = InternalId(uidGenerator.create(32))
  
}


case class InternalId(value: String)

