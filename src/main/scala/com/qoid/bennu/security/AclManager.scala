package com.qoid.bennu.security

import com.google.inject.Inject
import com.google.inject.Singleton
import com.qoid.bennu.model.id.AgentId
import m3.LockFreeMap
import m3.predef._

@Singleton
class AclManager @Inject()(injector: ScalaInjector) {
  private val agentAclManagers = LockFreeMap.empty[AgentId, AgentAclManager]

  def getAgentAclManager(agentId: AgentId): AgentAclManager = {
    agentAclManagers.getOrElseUpdate(agentId, new AgentAclManager(agentId, injector))
  }

  //TODO: Call from Delete Agent
  def removeAgentAclManager(agentId: AgentId): Unit = {
    agentAclManagers.remove(agentId)
  }
}
