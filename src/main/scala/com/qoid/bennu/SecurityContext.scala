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

object SecurityContext {

  case object SuperUserSecurityContext extends SecurityContext

  sealed trait AgentCapableSecurityContext extends SecurityContext
  
  case class AgentSecurityContext(agentId: AgentId) extends AgentCapableSecurityContext
  case class AliasSecurityContext(aliasIid: InternalId) extends AgentCapableSecurityContext
  case class ConnectionSecurityContext(connectionIid: InternalId) extends AgentCapableSecurityContext

  
  /**
   * Stupid implementation to convert a channelId into a SecurityContext.  Right now it assumes
   * all channels are agents.
   */
  def apply(channelId: ChannelId): SecurityContext = {
    val agentId = Agent.channelToAgentIdMap.get(channelId).getOrElse(throw new NotFoundException(s"no agent id found for ${channelId}"))
    AgentSecurityContext(agentId)
  }
  
  
  class ProviderSecurityContext @Inject() (
      provHttpReq: Provider[Option[Wrappers.Request]],
      provChannelId: Provider[Option[ChannelId]]
  ) extends Provider[SecurityContext] {
    def get: SecurityContext = {
      
      provHttpReq.
        get.
        flatMap { req =>
          req.parmValue("_channel").
            orElse(req.cookieValue("channel")).
            map(ChannelId.apply)
        }.
        orElse(provChannelId.get).
        map(chId=>SecurityContext(chId)).
        getOrElse(throw new ForbiddenException("unable to find a channel id that could be translated into an agent id"))
      
    }
  }
  
}


sealed trait SecurityContext {

}
