package com.qoid.bennu

import net.codingwell.scalaguice.ScalaModule
import com.google.inject.Provider
import com.google.inject.Module
import com.google.inject.util.Modules
import net.model3.guice.M3GuiceModule
import com.qoid.bennu.webservices.WebServicesModule
import net.model3.guice.bootstrap.ApplicationName
import net.model3.guice.bootstrap.ConfigurationDirectory
import net.model3.newfile.Directory
import com.google.inject.Provides
import net.model3.newfile.File
import javax.sql.DataSource
import java.sql.Connection
import net.model3.guice.ProviderJdbcConnectionViaTxn
import java.sql.DriverManager
import net.model3.guice.bootstrap.AbstractBootstrapper
import com.google.inject.Inject
import net.model3.logging.AutoLoggingConfigurator
import m3.Logger
import net.model3.lang.ClassX
import net.model3.collections.PropertiesX
import net.model3.util.Versioning
import net.model3.logging.Level
import m3.jdbc.Database
import m3.jdbc.M3ProviderDataSource
import m3.predef._
import m3.json.ConfigAssist

object GuiceModule {
  
  class Bootstrapper @Inject() (
    autoLoggingConfigurator: AutoLoggingConfigurator

  ) extends AbstractBootstrapper {
    
    lazy val logger = Logger.getLogger
    
    override def bootstrap = {
      autoLoggingConfigurator.apply()
    }
    

    @Override
    def postBootstrap() {
      PropertiesX.logProperties(System.getProperties(), Level.DEBUG );
      Versioning.logAllVersioningFound(Thread.currentThread().getContextClassLoader());

//      registerDriver("com.ibm.as400.access.AS400JDBCDriver");
      registerDriver("net.sf.log4jdbc.DriverSpy");
      registerDriver("com.mysql.jdbc.Driver");
//      registerDriver("org.postgresql.Driver");
//      registerDriver("org.hsqldb.jdbcDriver");

    }
    
    def registerDriver(driverClassname: String) {
      try {
        val clazz = ClassX.load(driverClassname);
        DriverManager.registerDriver(clazz.newInstance.asInstanceOf[java.sql.Driver]);
      } catch {
        case e: Exception => logger.warn("error registering " + driverClassname, e);
      }
    }

    
  }
  
}


class GuiceModule extends ScalaModule with Provider[Module] {
  
  lazy val logger = Logger.getLogger

  def get = 
    Modules.`override`(
      new M3GuiceModule()
    ).`with`(
      this, 
      new WebServicesModule()
    )

  def configure = {
    
    bind[ApplicationName].toInstance(new ApplicationName("bennu"))
    
    bind[net.model3.guice.bootstrap.Bootstrapper].to[GuiceModule.Bootstrapper]
    
    // we can get a more clever config directory later
    bind[ConfigurationDirectory].toInstance(new ConfigurationDirectory(new Directory(".")))
    
    bind[DataSource].toProvider[M3ProviderDataSource]
    bind[Connection].toProvider[ProviderJdbcConnectionViaTxn]
    
  }
  
  @Provides
  def config = {
    import JsonAssist._
    val possibleConfigFiles = List(
      new File(System.getProperty("config_file", "./config.json")),
      new File("config/config.json")
    )
    val file = possibleConfigFiles.find(_.exists).getOrError(s"unable to find a config file -- ${possibleConfigFiles}")
    val json = file.readText
    logger.debug(s"using config file ${file.getCanonicalPath} \n${json.indent("\t\t")}")
    ConfigAssist.parseHoconToJson(json).deserialize[Config]
  }
    
  @Provides
  def databaseConfig(config: Config): Database = config.database
    
  
}