package com.qoid.bennu

import scala.beans.BeanProperty
import com.google.inject.ProvidedBy
import com.google.inject.Provider
import javax.inject.Inject
import net.model3.xstream.XmlSerializableConfig
import m3.jdbc.Database
import m3.StringConverters

object Config {

  object Ring0Token extends StringConverters.HasStringConverter {
    val stringConverter = new StringConverters.Converter[Ring0Token] {
      override def toString(value: Ring0Token) = value.value
      def fromString(value: String) = Ring0Token(value)
    }    
  }
  
  case class Ring0Token(value: String)
  
}

case class Config(
    database: Database,
    ring0Token: Config.Ring0Token = Config.Ring0Token("i_am_canadian")
)
