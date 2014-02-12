package com.qoid.bennu.testclient.client

import com.qoid.bennu.JsonAssist.jsondsl._
import com.qoid.bennu.model._

trait ModelAssist {
  this: ChannelClient with ServiceAssist =>

  def createAlias(rootLabelIid: InternalId, name: String): Alias = {
    val profile = List("name" -> name, "imgSrc" -> "")
    upsert(Alias(agentId, rootLabelIid, name, profile))
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

  def getUberLabel(): Label = {
    query[Label]("name = 'uber label'").head
  }

  def getUberAlias(): Alias = {
    query[Alias]("name = 'uber alias'").head
  }
}
