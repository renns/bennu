package com.qoid.bennu.security

import m3.LockFreeMap
import m3.servlet.longpoll.ChannelId
import com.qoid.bennu.SecurityContext
import m3.predef._
import box._
import com.qoid.bennu.model.InternalId
import com.qoid.bennu.Config
import java.sql.{ Connection => JdbcConn }
import com.qoid.bennu.model.Agent
import com.qoid.bennu.model.Connection
import m3.servlet.longpoll.ChannelManager
object ChannelMap {

  val channelToSecurityContextMap = LockFreeMap[ChannelId,SecurityContext]()
  
  val config = inject[Config]
  val channelMgr = inject[ChannelManager]
  
  /**
   * Stupid implementation to convert a channelId into a SecurityContext.  Right now it assumes
   * all channels are agents.
   */
  def apply(channelId: ChannelId): Box[SecurityContext] = {
    channelToSecurityContextMap.get(channelId) ?~ s"no agent id found for ${channelId}"
  }

  def authenticate(iid: InternalId)(implicit conn: JdbcConn): Box[ChannelId] = {

    config.
      superUserAuthTokens.
      find(_.value =:= iid.value).
      map(_=>SecurityContext.SuperUserSecurityContext).
      orElse(
        Agent.fetchOpt(iid).map(a=>SecurityContext.AgentSecurityContext(a.agentId))
      ).orElse (
        Connection.fetchOpt(iid).map(c=>SecurityContext.ConnectionSecurityContext(iid))
      ).map { sc =>
        val channel = channelMgr.createChannel()
        channelToSecurityContextMap += channel.id -> sc
        channel.id
      } ?~ s"failed to authenticate ${iid.value}"

  }

}
