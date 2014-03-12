package com.qoid.bennu.testclient.client

import com.qoid.bennu.JsonAssist.jsondsl._
import com.qoid.bennu.model._
import m3.jdbc._

trait ModelAssist {
  this: ChannelClient with ServiceAssist =>

  def createAlias(rootLabelIid: InternalId, name: String): Alias = {
    val profile = ("name" -> name) ~ ("imgSrc" -> "")
    upsert(Alias(rootLabelIid, profile))
  }

  def createConnection(aliasId: InternalId, localPeerId: PeerId, remotePeerId: PeerId): Connection = {
    val label = upsert(Label("connection"))
    upsert(Connection(aliasId, label.iid, localPeerId, remotePeerId))
  }

  def createLabel(name: String): Label = {
    upsert(Label(name))
  }

  def createChildLabel(parentLabelIid: InternalId, childLabelName: String): Label = {
    val childLabel = createLabel(childLabelName)
    createLabelChild(parentLabelIid, childLabel.iid)
    childLabel
  }

  def createLabelChild(parentIid: InternalId, childIid: InternalId): LabelChild = {
    upsert(LabelChild(parentIid, childIid))
  }

  def getRootLabel(): Label = {
    val alias = getRootAlias()
    queryLocal[Label](sql"iid = ${alias.rootLabelIid}").head
  }

  def getRootAlias(): Alias = {
    queryLocal[Alias](sql"iid = $rootAliasIid").head
  }
}
