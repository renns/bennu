package com.qoid.bennu

import scala.beans.BeanProperty
import com.google.inject.ProvidedBy
import com.google.inject.Provider
import javax.inject.Inject
import net.model3.xstream.XmlSerializableConfig
import m3.jdbc.Database
import m3.StringConverters

object Config {

  object SuperUserAuthToken extends StringConverters.HasStringConverter {
    val stringConverter = new StringConverters.Converter[SuperUserAuthToken] {
      override def toString(value: SuperUserAuthToken) = value.value
      def fromString(value: String) = SuperUserAuthToken(value)
    }    
  }
  
  case class SuperUserAuthToken(value: String)
  
}

case class Config(
    database: Database,
    superUserAuthTokens: List[Config.SuperUserAuthToken] = List(Config.SuperUserAuthToken("i_am_canadian"))
)
