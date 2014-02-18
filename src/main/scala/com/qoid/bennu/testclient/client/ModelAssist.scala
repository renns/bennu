package com.qoid.bennu.testclient.client

import com.qoid.bennu.JsonAssist.jsondsl._
import com.qoid.bennu.model._
import m3.jdbc._

trait ModelAssist {
  this: ChannelClient with ServiceAssist =>

  def createAlias(rootLabelIid: InternalId, name: String): Alias = {
    val profile = ("name" -> name) ~ ("imgSrc" -> "")
    upsert(Alias(agentId, rootLabelIid, profile))
  }

  def createConnection(aliasId: InternalId, localPeerId: PeerId, remotePeerId: PeerId): Connection = {
    upsert(Connection(agentId, aliasId, localPeerId, remotePeerId))
  }

  def createLabel(name: String): Label = {
    upsert(Label(agentId, name))
  }

  def createChildLabel(parentLabelIid: InternalId, childLabelName: String): Label = {
    val childLabel = createLabel(childLabelName)
    createLabelChild(parentLabelIid, childLabel.iid)
    childLabel
  }

  def createLabelChild(parentIid: InternalId, childIid: InternalId): LabelChild = {
    upsert(LabelChild(agentId, parentIid, childIid))
  }

  def getRootLabel(): Label = {
    val alias = getUberAlias()
    query[Label](sql"iid = ${alias.rootLabelIid}").head
  }

  def getUberAlias(): Alias = {
    val agent = query[Agent](sql"iid = $agentId").head
    query[Alias](sql"iid = ${agent.uberAliasIid}").head
  }
}
