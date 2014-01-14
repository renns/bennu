package com.ahsrcm.entdb

import scala.beans.BeanProperty
import net.model3.guice.DatabaseConfig
import com.thoughtworks.xstream.annotations.XStreamAlias
import com.google.inject.ProvidedBy
import com.google.inject.Provider
import javax.inject.Inject
import net.model3.xstream.XmlSerializableConfig

object Config {
    
  /**
   * A much simplified database config.  We can fill it out later as we need more features
   * from DatabaseConfig exposed.
   */
  case class Database(
    jdbcUrl: String,
    driverClass: String,
    user: Option[String],
    password: Option[String],
    validationQuery: String,
    initialConnections: Int,
    maxConnections: Int
  ) {
    
    lazy val databaseConfig: DatabaseConfig = new DatabaseConfig {
      setJdbcUrl(jdbcUrl)
      setDriver(driverClass)
      user.foreach(setUser)
      password.foreach(setPassword)
//      setDriver(driverClass)
      getPoolConfig.setInitialSize(initialConnections)
      getPoolConfig().setMaxActive(maxConnections)
      getPoolConfig().setValidationQuery(validationQuery)
    }
    
  }
}

case class Config(
    database: Config.Database
)
