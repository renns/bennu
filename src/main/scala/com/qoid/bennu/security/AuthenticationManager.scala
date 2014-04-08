package com.qoid.bennu.security

import com.google.inject.Inject
import com.google.inject.Singleton
import com.qoid.bennu.model._
import com.qoid.bennu.model.id._
import java.security.MessageDigest
import java.security.SecureRandom
import java.sql.{ Connection => JdbcConn }
import m3.jdbc._
import m3.predef._
import m3.predef.box._
import sun.misc.BASE64Decoder
import sun.misc.BASE64Encoder

@Singleton
class AuthenticationManager @Inject()(injector: ScalaInjector) {
  def createLogin(aliasIid: InternalId, password: String): Login = {
    val av = injector.instance[AgentView]

    val alias = av.fetch[Alias](aliasIid)
    val agent = av.selectOne[Agent]("")
    val authenticationId = AuthenticationId(s"${agent.name.toLowerCase}.${alias.name.toLowerCase}")

    //TODO: Validate password strength
    val salt = generateSalt()
    val hash = generateHash(password, salt)

    av.insert[Login](Login(aliasIid, authenticationId, hash, salt))
  }

  def authenticate(authenticationId: AuthenticationId, password: String): Box[InternalId] = {
    implicit val jdbcConn = injector.instance[JdbcConn]

    val login = Login.selectOpt(sql"authenticationId = ${authenticationId.value.toLowerCase}").orElse {
      for {
        agent <- Agent.selectOpt(sql"LCASE(name) = ${authenticationId.value.toLowerCase}")
        alias <- Alias.fetchOpt(agent.uberAliasIid)
        login <- Login.selectOpt(sql"aliasIid = ${alias.iid}")
      } yield login
    }

    login.flatMap { l =>
      val hash = generateHash(password, l.salt)
      if (hash == l.passwordHash) Some(l.aliasIid) else None
    } ?~ s"failed to authenticate $authenticationId"
  }

  private def generateHash(password: String, salt: String): String = {
    val saltBytes = new BASE64Decoder().decodeBuffer(salt)
    val digest = MessageDigest.getInstance("SHA-512")
    digest.reset()
    digest.update(saltBytes)
    val hash = digest.digest(password.getBytes("UTF-8"))
    new BASE64Encoder().encode(hash)
  }

  private def generateSalt(): String = {
    val sr = SecureRandom.getInstance("SHA1PRNG")
    val salt = new Array[Byte](16)
    sr.nextBytes(salt)
    new BASE64Encoder().encode(salt)
  }
}
