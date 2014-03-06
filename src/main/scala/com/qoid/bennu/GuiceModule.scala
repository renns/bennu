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
import m3.json.JsonSerializer
import com.qoid.bennu.model.AgentId
import com.google.inject.Singleton
import net.model3.transaction.TransactionManager
import com.qoid.bennu.SecurityContext.ProviderSecurityContext
import m3.servlet.beans.Wrappers
import m3.servlet.beans.guice.ProviderOptionalRequest
import m3.servlet.longpoll.ChannelId
import m3.servlet.longpoll.GuiceProviders.ProviderOptionalChannelId
import m3.servlet.longpoll.GuiceProviders.ProviderChannelId
import com.qoid.bennu.SecurityContext.AgentCapableSecurityContext
import com.qoid.bennu.SecurityContext.ProviderAgentCapableSecurityContext
import m3.servlet.longpoll.JettyChannelManager
import m3.servlet.longpoll.ChannelManager
import com.qoid.bennu.SecurityContext.BennuProviderOptionChannelId
import com.qoid.bennu.SecurityContext.BennuProviderChannelId
import net.model3.guice.LifeCycleManager
import net.model3.guice.LifeCycleListeners
import com.qoid.bennu.util.HsqldbServerStarterUpper
import com.qoid.bennu.SecurityContext.ProviderAgentView
import net.codingwell.scalaguice.InjectorExtensions.ScalaInjector
import m3.guice.ScalaInjectorProvider
import m3.servlet.M3ServletGuiceModule

object GuiceModule {
  
  class Bootstrapper @Inject() (
    autoLoggingConfigurator: AutoLoggingConfigurator,
    lifeCycle: LifeCycleManager
  ) extends AbstractBootstrapper {
    
    lazy val logger = Logger.getLogger
    
    override def bootstrap = {
      autoLoggingConfigurator.apply()

      lifeCycle.config.add(new LifeCycleListeners.Config {
        def configComplete = {
          // tickle the hsqldb server
          inject[HsqldbServerStarterUpper]
          PropertiesX.logProperties(System.getProperties(), Level.DEBUG );
          Versioning.logAllVersioningFound(Thread.currentThread().getContextClassLoader());
        }
      })

    }
    
  }
  
  @Singleton
  class ProviderAgentId @Inject() (
    txnManager: TransactionManager
  ) extends Provider[AgentId] {
    def get = Option(txnManager.getTransaction.getAttribute[AgentId](classOf[AgentId].getName())).getOrError("no agent id found")
  }

}


class GuiceModule extends ScalaModule with Provider[Module] {
  
  lazy val logger = Logger.getLogger

  def get = 
    Modules.`override`(
      new M3GuiceModule(),
      new M3ServletGuiceModule()
    ).`with`(
      this, 
      new WebServicesModule()
    )

  def configure = {
    
    bind[ApplicationName].toInstance(new ApplicationName("bennu"))
    
    bind[net.model3.guice.bootstrap.Bootstrapper].to[GuiceModule.Bootstrapper]
    
    // we can get a more clever config directory later
    bind[ConfigurationDirectory].toInstance(new ConfigurationDirectory(new Directory(".")))
    
    bind[ScalaInjector].toProvider[ScalaInjectorProvider]
    
    bind[DataSource].toProvider[M3ProviderDataSource]
    bind[Connection].toProvider[ProviderJdbcConnectionViaTxn]
    bind[Option[Wrappers.Request]].toProvider[ProviderOptionalRequest]
    bind[ChannelId].toProvider[BennuProviderChannelId]
    bind[Option[ChannelId]].toProvider[BennuProviderOptionChannelId]

    bind[SecurityContext].toProvider[ProviderSecurityContext]
    bind[AgentCapableSecurityContext].toProvider[ProviderAgentCapableSecurityContext]
    bind[AgentView].toProvider[ProviderAgentView]
    
    bind[ChannelManager].to[JettyChannelManager]

  }
  
  @Provides
  def config = {
    val possibleConfigFiles = List(
      new File(System.getProperty("config_file", "./config.json")),
      new File("config/config.json")
    )
    val file = possibleConfigFiles.find(_.exists).getOrError(s"unable to find a config file -- ${possibleConfigFiles}")
    val json = file.readText
    logger.debug(s"using config file ${file.getCanonicalPath} \n${json.indent("\t\t")}")
    import JsonAssist._
    ConfigAssist.parseHoconToJson(json).deserialize[Config]
  }
    
  @Provides
  def databaseConfig(config: Config): Database = config.database
    
  @Provides 
  def jsonSerializer = JsonAssist.serializer

  
}