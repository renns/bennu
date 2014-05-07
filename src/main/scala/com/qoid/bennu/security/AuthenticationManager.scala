package com.qoid.bennu.security

import com.google.inject.Inject
import com.google.inject.Singleton
import com.qoid.bennu.Config
import com.qoid.bennu.model._
import com.qoid.bennu.model.id._
import java.sql.{ Connection => JdbcConn }
import m3.jdbc._
import m3.predef._
import m3.predef.box._
import org.mindrot.jbcrypt.BCrypt

@Singleton
class AuthenticationManager @Inject()(injector: ScalaInjector, config: Config) {
  def createLogin(aliasIid: InternalId, password: String): Login = {
    val av = injector.instance[AgentView]

    val alias = av.fetch[Alias](aliasIid)
    val agent = av.selectOne[Agent]("")
    val authenticationId = AuthenticationId(s"${agent.name.toLowerCase}.${alias.name.toLowerCase}")

    //TODO: Validate password strength

    val salt = BCrypt.gensalt(config.bcryptSaltRounds)
    val hash = BCrypt.hashpw(password, salt)

    av.insert[Login](Login(aliasIid, authenticationId, hash))
  }

  def updateAuthenticationId(aliasIid: InternalId): Unit = {
    val av = injector.instance[AgentView]

    val alias = av.fetch[Alias](aliasIid)
    val agent = av.selectOne[Agent]("")

    av.selectOpt[Login](sql"aliasIid = $aliasIid").foreach { login =>
      av.update(login.copy(authenticationId = AuthenticationId(s"${agent.name.toLowerCase}.${alias.name.toLowerCase}")))
    }
  }

  def authenticate(authenticationId: AuthenticationId, password: String): Box[InternalId] = {
    implicit val jdbcConn = injector.instance[JdbcConn]

    val login = Login.selectOpt(sql"authenticationId = ${authenticationId.value.toLowerCase}").orElse {
      for {
        agent <- Agent.selectOpt(sql"lower(name) = ${authenticationId.value.toLowerCase}")
        alias <- Alias.fetchOpt(agent.uberAliasIid)
        login <- Login.selectOpt(sql"aliasIid = ${alias.iid}")
      } yield login
    }

    login.flatMap { l =>
      if (BCrypt.checkpw(password, l.passwordHash)) Some(l.aliasIid) else None
    } ?~ s"failed to authenticate $authenticationId"
  }
}
