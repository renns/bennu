package com.qoid.bennu.testclient.client

import com.qoid.bennu.JsonAssist.jsondsl._
import com.qoid.bennu.model._
import m3.jdbc._

trait ModelAssist {
  this: ChannelClient with ServiceAssist =>

  def createAlias(rootLabelIid: InternalId, name: String): Alias = {
    val profile = List("name" -> name, "imgSrc" -> "")
    upsert(Alias(agentId, rootLabelIid, profile))
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
    query[Label](sql"""name = 'uber label'""").head
  }

  def getUberAlias(): Alias = {
    query[Alias](sql"""json_str(profile, 'name') = 'Uber Alias'""").head
  }
}
