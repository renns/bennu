package com.qoid.bennu.testclient.client

import com.qoid.bennu.model._

trait ModelAssist {
  this: ChannelClient with ServiceAssist =>

  def createAlias(rootLabelIid: InternalId, name: String): Alias = {
    upsert(Alias(agentId, rootLabelIid, name))
  }

  def createConnection(aliasId: InternalId, localPeerId: PeerId, remotePeerId: PeerId): Connection = {
    upsert(Connection(agentId, aliasId, localPeerId, remotePeerId))
  }

  def createLabel(name: String): Label = {
    upsert(Label(agentId, name))
  }

  def createLabelChild(parentIid: InternalId, childIid: InternalId): LabelChild = {
    upsert(LabelChild(agentId, parentIid, childIid))
  }
}
