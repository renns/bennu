package com.qoid.bennu

import com.qoid.bennu.model.AgentId
import com.qoid.bennu.model.InternalId
import com.google.inject.Provider
import com.google.inject.Inject
import m3.servlet.beans.Wrappers
import m3.servlet.longpoll.ChannelId
import m3.servlet.longpoll.ChannelManager
import m3.predef._
import com.qoid.bennu.model.Agent
import m3.servlet.ForbiddenException
import m3.servlet.NotFoundException
import m3.servlet.longpoll.GuiceProviders.ProviderOptionalChannelId
import com.google.inject.Singleton

object SecurityContext {

  case object SuperUserSecurityContext extends SecurityContext

  sealed trait AgentCapableSecurityContext extends SecurityContext {
    override def optAgentId: Option[AgentId] = Some(agentId)
    def agentId: AgentId
  }

  case class AgentSecurityContext(agentId: AgentId) extends AgentCapableSecurityContext
//  case class AliasSecurityContext(aliasIid: InternalId) extends AgentCapableSecurityContext
//  case class ConnectionSecurityContext(connectionIid: InternalId) extends AgentCapableSecurityContext

  
  /**
   * Stupid implementation to convert a channelId into a SecurityContext.  Right now it assumes
   * all channels are agents.
   */
  def apply(channelId: ChannelId): SecurityContext = {
    val agentId = Agent.channelToAgentIdMap.get(channelId).getOrElse(throw new NotFoundException(s"no agent id found for ${channelId}"))
    AgentSecurityContext(agentId)
  }
  
  
  @Singleton
  class ProviderSecurityContext @Inject() (
      provChannelId: Provider[Option[ChannelId]]
  ) extends Provider[SecurityContext] {
    def get: SecurityContext = {
      val ch = provChannelId.get
      ch.map(chId=>SecurityContext(chId)).
        getOrElse(throw new ForbiddenException("unable to find a channel id that could be translated into an agent id"))
      
    }
  }

  class BennuProviderChannelId @Inject() (
    provOptChannelId: Provider[Option[ChannelId]]
  ) extends Provider[ChannelId] {
    def get = provOptChannelId.get.getOrError("unable to find channel id")
  }

  @Singleton
  class BennuProviderOptionChannelId @Inject() (
      provHttpReq: Provider[Option[Wrappers.Request]],
      provChannelId: ProviderOptionalChannelId
  ) extends Provider[Option[ChannelId]] {
    def get = {
      provHttpReq.
        get.
        flatMap { req =>
          req.parmValue("_channel").
            orElse(req.cookieValue("channel")).
            map(ChannelId.apply)
        }.
        orElse(provChannelId.get)
    }
  }

  class ProviderAgentCapableSecurityContext @Inject() (
      provSecurityContext: Provider[SecurityContext]
  ) extends Provider[AgentCapableSecurityContext] {
    def get: AgentCapableSecurityContext = {
      provSecurityContext.get match {
        case a: AgentCapableSecurityContext => a
        case s => m3x.error("found ${s} required AgentCapableSecurityContext")
      }
    }
  }
  
}


sealed trait SecurityContext {
  def optAgentId: Option[AgentId] = None
}
