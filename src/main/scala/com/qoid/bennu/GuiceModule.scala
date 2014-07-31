package com.qoid.bennu

import java.sql.Connection
import javax.sql.DataSource

import com.google.inject.Inject
import com.google.inject.Module
import com.google.inject.Provider
import com.google.inject.Provides
import com.google.inject.Scopes
import com.google.inject.Singleton
import com.google.inject.util.Modules
import com.qoid.bennu.GuiceModule.BennuProviderChannelId
import com.qoid.bennu.GuiceModule.BennuProviderOptionChannelId
import com.qoid.bennu.GuiceModule.ProviderAgentAclManager
import com.qoid.bennu.GuiceModule.ProviderSecurityContext
import com.qoid.bennu.GuiceModule.ProviderSession
import com.qoid.bennu.distributed.MessageQueue
import com.qoid.bennu.distributed.SimpleMessageQueue
import com.qoid.bennu.security.AclManager
import com.qoid.bennu.security.AgentAclManager
import com.qoid.bennu.security.SecurityContext
import com.qoid.bennu.session.Session
import com.qoid.bennu.session.SessionManager
import com.qoid.bennu.util.ConfigAssist
import com.qoid.bennu.util.HsqldbServerStarterUpper
import com.qoid.bennu.webservices.WebServicesModule
import m3.Logger
import m3.guice.ScalaInjectorProvider
import m3.jdbc.Database
import m3.jdbc.M3ProviderDataSource
import m3.jdbc.mapper.ColumnMapper.ColumnMapperFactory
import m3.predef._
import m3.servlet.M3ServletGuiceModule
import m3.servlet.beans.Wrappers
import m3.servlet.beans.guice.ProviderOptionalRequest
import m3.servlet.longpoll.ChannelId
import m3.servlet.longpoll.GuiceProviders.ProviderOptionalChannelId
import net.codingwell.scalaguice.ScalaModule
import net.model3.collections.PropertiesX
import net.model3.guice.LifeCycleListeners
import net.model3.guice.LifeCycleManager
import net.model3.guice.M3GuiceModule
import net.model3.guice.ProviderJdbcConnectionViaTxn
import net.model3.guice.bootstrap.AbstractBootstrapper
import net.model3.guice.bootstrap.ApplicationName
import net.model3.guice.bootstrap.ConfigurationDirectory
import net.model3.logging.AutoLoggingConfigurator
import net.model3.logging.Level
import net.model3.newfile.Directory
import net.model3.newfile.File
import net.model3.transaction.Transaction
import net.model3.util.Versioning

object GuiceModule {
  
  class Bootstrapper @Inject() (
    autoLoggingConfigurator: AutoLoggingConfigurator,
    lifeCycle: LifeCycleManager
  ) extends AbstractBootstrapper {
    
    lazy val logger = Logger.getLogger
    
    override def bootstrap() = {
      autoLoggingConfigurator.apply()

      lifeCycle.config.add(new LifeCycleListeners.Config {
        def configComplete() = {
          // tickle the hsqldb server
          inject[HsqldbServerStarterUpper]
          PropertiesX.logProperties(System.getProperties, Level.DEBUG )
          Versioning.logAllVersioningFound(Thread.currentThread().getContextClassLoader)
        }
      })
    }
  }

  @Singleton
  class ProviderSession @Inject() (
    provChannelId: Provider[ChannelId],
    sessionMgr: SessionManager
  ) extends Provider[Session] {

    def get: Session = {
      sessionMgr.getSession(provChannelId.get())
    }
  }

  @Singleton
  class ProviderSecurityContext @Inject() (
    provSession: Provider[Session],
    provTxn: Provider[Transaction]
  ) extends Provider[SecurityContext] {

    val attrName = classOf[SecurityContext].getName
    def get: SecurityContext = {
      provTxn.get.getAttribute[SecurityContext](attrName, true) match {
        case null => provSession.get().securityContext
        case sc => sc
      }
    }
  }

  @Singleton
  class BennuProviderChannelId @Inject() (
    provOptChannelId: Provider[Option[ChannelId]]
  ) extends Provider[ChannelId] {

    def get: ChannelId = provOptChannelId.get.getOrError("unable to find channel id")
  }

  @Singleton
  class BennuProviderOptionChannelId @Inject() (
    provHttpReq: Provider[Option[Wrappers.Request]],
    provChannelId: ProviderOptionalChannelId
  ) extends Provider[Option[ChannelId]] {

    def get: Option[ChannelId] = {
      provHttpReq.get().
        flatMap(_.headerValue("Qoid-ChannelId")).
        map(ChannelId.apply).
        orElse(provChannelId.get())
    }
  }

  @Singleton
  class ProviderAgentAclManager @Inject() (
    aclMgr: AclManager,
    provSecurityContext: Provider[SecurityContext]
  ) extends Provider[AgentAclManager] {

    def get: AgentAclManager = aclMgr.getAgentAclManager(provSecurityContext.get().agentId)
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

  def configure() = {
    
    bind[ApplicationName].toInstance(new ApplicationName("bennu"))
    
    bind[net.model3.guice.bootstrap.Bootstrapper].to[GuiceModule.Bootstrapper]
    
    // we can get a more clever config directory later
    bind[ConfigurationDirectory].toInstance(new ConfigurationDirectory(new Directory(".")))
    
    bind[ScalaInjector].toProvider[ScalaInjectorProvider]
    
    bind[DataSource].toProvider[M3ProviderDataSource].in(Scopes.SINGLETON)
    bind[Connection].toProvider[ProviderJdbcConnectionViaTxn]

    bind[Option[Wrappers.Request]].toProvider[ProviderOptionalRequest]

    bind[ChannelId].toProvider[BennuProviderChannelId]
    bind[Option[ChannelId]].toProvider[BennuProviderOptionChannelId]
    bind[Session].toProvider[ProviderSession]
    bind[SecurityContext].toProvider[ProviderSecurityContext]
    bind[AgentAclManager].toProvider[ProviderAgentAclManager]

    bind[MessageQueue].to[SimpleMessageQueue]

    bind[ColumnMapperFactory].toInstance(JdbcAssist.columnMapper)
  }
  
  @Provides
  def config = {
    val possibleConfigFiles = List(
      new File(System.getProperty("config_file", "./config.json")),
      new File("config/config.json")
    )
    val file = possibleConfigFiles.find(_.exists).getOrError(s"unable to find a config file -- ${possibleConfigFiles}")
    val json = file.readText
    logger.debug(s"using config file ${file.getCanonicalPath}")
    import com.qoid.bennu.JsonAssist._
    ConfigAssist.parseHoconToJson(json).deserialize[Config]
  }
    
  @Provides
  def databaseConfig(config: Config): Database = config.database
    
  @Provides 
  def jsonSerializer = JsonAssist.serializer
}
