package com.qoid.bennu.security

import com.google.inject.Inject
import com.google.inject.Singleton
import com.qoid.bennu.BennuException
import com.qoid.bennu.Config
import com.qoid.bennu.ErrorCode
import com.qoid.bennu.model.Agent
import com.qoid.bennu.model.Alias
import com.qoid.bennu.model.Label
import com.qoid.bennu.model.Login
import com.qoid.bennu.model.id.AuthenticationId
import com.qoid.bennu.model.id.InternalId
import m3.jdbc._
import m3.predef._
import org.mindrot.jbcrypt.BCrypt

@Singleton
class AuthenticationManager @Inject()(injector: ScalaInjector, config: Config) {
  def validatePassword(password: String): Unit = {
    if (password.isEmpty) {
      throw new BennuException(ErrorCode.passwordInvalid)
    }

    //TODO: Validate password strength
  }

  def createLogin(aliasIid: InternalId, password: String): Login = {
    validatePassword(password)

    val alias = Alias.fetch(aliasIid)
    val label = Label.fetch(alias.labelIid)
    val agent = Agent.selectOne("")
    val authenticationId = AuthenticationId(s"${agent.name.toLowerCase}.${label.name.toLowerCase}")

    val salt = BCrypt.gensalt(config.bcryptSaltRounds)
    val hash = BCrypt.hashpw(password, salt)

    Login.insert(Login(aliasIid, authenticationId, hash))
  }

  def updateAuthenticationId(aliasIid: InternalId): Unit = {
    val alias = Alias.fetch(aliasIid)
    val label = Label.fetch(alias.labelIid)
    val agent = Agent.selectOne("")

    Login.selectOpt(sql"aliasIid = $aliasIid").foreach { login =>
      Login.update(login.copy(authenticationId = AuthenticationId(s"${agent.name.toLowerCase}.${label.name.toLowerCase}")))
    }
  }

  def authenticate(authenticationId: AuthenticationId, password: String): InternalId = {
    SystemSecurityContext {
      val loginOpt = Login.selectOpt(sql"authenticationId = ${authenticationId.value.toLowerCase}").orElse {
        for {
          agent <- Agent.selectOpt(sql"lower(name) = ${authenticationId.value.toLowerCase}")
          alias <- Alias.fetchOpt(agent.aliasIid)
          login <- Login.selectOpt(sql"aliasIid = ${alias.iid}")
        } yield login
      }

      loginOpt match {
        case Some(login) =>
          if (BCrypt.checkpw(password, login.passwordHash)) {
            Alias.fetch(login.aliasIid).connectionIid
          } else {
            throw new BennuException(ErrorCode.authenticationFailed)
          }
        case _ => throw new BennuException(ErrorCode.authenticationFailed)
      }
    }
  }
}
