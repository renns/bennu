package com.qoid.bennu.security

import com.qoid.bennu.Config
import com.qoid.bennu.SecurityContext
import com.qoid.bennu.model.Agent
import com.qoid.bennu.model.Alias
import com.qoid.bennu.model.InternalId
import java.sql.{ Connection => JdbcConn }
import m3.LockFreeMap
import m3.jdbc._
import m3.predef._
import m3.predef.box._
import m3.servlet.longpoll.ChannelId
import m3.servlet.longpoll.ChannelManager

object ChannelMap {

  val namePattern = "[^.]+"

  val channelToSecurityContextMap = LockFreeMap[ChannelId,SecurityContext]()
  
  val config = inject[Config]
  val channelMgr = inject[ChannelManager]

  def apply(channelId: ChannelId): Box[SecurityContext] = {
    channelToSecurityContextMap.get(channelId) ?~ s"no security context found for ${channelId}"
  }

  def authenticate(authenticationId: String)(implicit conn: JdbcConn): Box[(ChannelId, InternalId)] = {
    val pattern1 = s"($namePattern)".r
    val pattern2 = s"($namePattern)\\.($namePattern)".r

    authenticationId match {
      case pattern1(agentName) =>
        (
          for {
            agent <- Agent.selectOpt(sql"name = $agentName")
            alias <- Alias.fetchOpt(agent.uberAliasIid)
          } yield (createChannel(alias), alias.iid)
        ) ?~ s"failed to authenticate $authenticationId"
      case pattern2(agentName, aliasName) =>
        (
          for {
            agent <- Agent.selectOpt(sql"name = $agentName")
            alias <- Alias.selectOpt(sql"name = $aliasName and agentId = ${agent.agentId}")
          } yield (createChannel(alias), alias.iid)
        ) ?~ s"failed to authenticate $authenticationId"
      case _ => Failure(s"failed to authenticate $authenticationId")
    }
  }

  private def createChannel(alias: Alias): ChannelId = {
    val sc = SecurityContext.AliasSecurityContext(alias.iid)
    val channel = channelMgr.createChannel()
    channelToSecurityContextMap += channel.id -> sc
    channel.id
  }
}
