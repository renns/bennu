package com.qoid.bennu

import m3.StringConverters
import m3.jdbc.Database

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
  superUserAuthTokens: List[Config.SuperUserAuthToken] = List(Config.SuperUserAuthToken("i_am_canadian")),
  startHsqldbTooling: Boolean = true,
  bcryptSaltRounds: Int = 10,
  amqpUri: String = "amqp://localhost"
)
