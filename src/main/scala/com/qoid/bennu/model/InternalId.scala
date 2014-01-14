package com.qoid.bennu.model

import m3.StringConverters.HasStringConverter
import m3.StringConverters.Converter

object InternalId extends HasStringConverter {
  
  val stringConverter = new Converter[InternalId] {
    override def toString(value: InternalId) = value.value
    def fromString(value: String) = InternalId(value)
  }

}


case class InternalId(value: String)

