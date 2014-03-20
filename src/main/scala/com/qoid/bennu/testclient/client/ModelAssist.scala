package com.qoid.bennu.testclient.client

import com.qoid.bennu.model._
import com.qoid.bennu.model.id._
import m3.jdbc._

trait ModelAssist {
  this: ChannelClient with ServiceAssist =>

  def createAlias(parentLabelIid: InternalId, name: String, imgSrc: String): Alias = {
    val label = createChildLabel(parentLabelIid, name)
    val alias = upsert(Alias(label.iid, name))
    upsert(Profile(alias.iid, name, imgSrc))
    alias
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

  def createProfile(aliasIid: InternalId, name: String, imgSrc: String): Profile = {
    upsert(Profile(aliasIid, name, imgSrc))
  }

  def getRootLabel(): Label = {
    val alias = getRootAlias()
    queryLocal[Label](sql"iid = ${alias.rootLabelIid}").head
  }

  def getRootAlias(): Alias = {
    queryLocal[Alias](sql"iid = $rootAliasIid").head
  }

  def getProfile(aliasIid: InternalId): Profile = {
    queryLocal[Profile](sql"aliasIid = $aliasIid").head
  }
}
