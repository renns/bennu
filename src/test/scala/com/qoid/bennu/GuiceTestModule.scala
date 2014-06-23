package com.qoid.bennu

import net.codingwell.scalaguice.ScalaModule
import com.google.inject.Provider
import com.google.inject.Module
import com.google.inject.util.Modules
import net.model3.guice.bootstrap.ApplicationName
import com.google.inject.Provides
import m3.Logger
import m3.jdbc.Database

object GuiceTestModule {
  
}


class GuiceTestModule extends ScalaModule with Provider[Module] {
  
  lazy val logger = Logger.getLogger

  def get = 
    Modules.`override`(
      new GuiceModule().get
    ).`with`(
      this
    )

  def configure = {
    
    bind[ApplicationName].toInstance(new ApplicationName("tests"))

  }
    
  @Provides
  lazy val databaseConfig = Database(
    jdbcUrl = "jdbc:hsqldb:mem:bennu-test",
    driverClass = None,
    user = Some("sa"),
    password = Some(""),
    validationQuery = "select 1+1"
  )

}
