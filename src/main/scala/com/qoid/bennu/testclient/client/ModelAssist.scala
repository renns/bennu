package com.qoid.bennu.testclient.client

import com.qoid.bennu.JsonAssist.jsondsl._
import com.qoid.bennu.model._
import com.qoid.bennu.model.id._
import m3.jdbc._

trait ModelAssist {
  this: ChannelClient with ServiceAssist =>

  def createAlias(parentIid: InternalId, name: String, profileName: Option[String] = None, profileImgSrc: Option[String] = None): Alias = {
    upsert(Alias(name), Some(parentIid), profileName, profileImgSrc)
  }

  def createConnection(aliasIid: InternalId, localPeerId: PeerId, remotePeerId: PeerId): Connection = {
    upsert(Connection(aliasIid, localPeerId, remotePeerId))
  }

  def createLabel(parentIid: InternalId, name: String): Label = {
    upsert(Label(name, data = "color" -> "#7F7F7F"), Some(parentIid))
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
