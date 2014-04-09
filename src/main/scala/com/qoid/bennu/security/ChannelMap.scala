package com.qoid.bennu.security

import com.qoid.bennu.Config
import com.qoid.bennu.model.id._
import m3.LockFreeMap
import m3.predef._
import m3.predef.box._
import m3.servlet.longpoll.ChannelId
import m3.servlet.longpoll.ChannelManager

object ChannelMap {
  val channelToSecurityContextMap = LockFreeMap[ChannelId,SecurityContext]()
  val injector = inject[ScalaInjector]
  val config = injector.instance[Config]
  val channelMgr = injector.instance[ChannelManager]
  val authMgr = injector.instance[AuthenticationManager]

  def apply(channelId: ChannelId): Box[SecurityContext] = {
    channelToSecurityContextMap.get(channelId) ?~ s"no security context found for ${channelId}"
  }

  def authenticate(authenticationId: AuthenticationId, password: String): Box[(ChannelId, InternalId)] = {
    authMgr.authenticate(authenticationId, password).map { aliasIid =>
      val channel = channelMgr.createChannel()
      channelToSecurityContextMap += channel.id -> AliasSecurityContext(injector, aliasIid)
      (channel.id, aliasIid)
    } ?~ s"failed to authenticate $authenticationId"
  }
}
