package com.qoid.bennu.testclient.client

import com.qoid.bennu.JsonAssist._
import com.qoid.bennu.JsonAssist.jsondsl._
import com.qoid.bennu.model._
import com.qoid.bennu.model.id._
import m3.jdbc._

trait ModelAssist {
  this: ChannelClient with ServiceAssist =>

  def createAlias(parentIid: InternalId, name: String, profileName: Option[String] = None, profileImgSrc: Option[String] = None): Alias = {
    upsert(Alias(name), Some(parentIid), profileName, profileImgSrc)
  }

  def updateAlias(iid: InternalId, name: String): Unit = {
    val alias = queryLocal[Alias](sql"iid = $iid").head
    upsert(alias.copy(name = name))
  }

  def updateProfile(aliasIid: InternalId, name: String, imgSrc: String): Unit = {
    val profile = queryLocal[Profile](sql"aliasIid = $aliasIid").head
    upsert(profile.copy(name = name, imgSrc = imgSrc))
  }

  def deleteAlias(iid: InternalId): Unit = {
    delete[Alias](iid)
  }

  def createConnection(aliasIid: InternalId, localPeerId: PeerId, remotePeerId: PeerId): Connection = {
    upsert(Connection(aliasIid, localPeerId, remotePeerId))
  }

  def createContent(aliasIid: InternalId, contentType: String, data: JValue, labelIids: Option[List[InternalId]] = None): Content = {
    upsert(Content(aliasIid, contentType, data = data), labelIids = labelIids)
  }

  def updateContent(iid: InternalId, data: JValue): Unit = {
    val content = queryLocal[Content](sql"iid = $iid").head
    upsert(content.copy(data = data))
  }

  def addLabelToContent(contentIid: InternalId, labelIid: InternalId): Unit = {
    upsert(LabeledContent(contentIid, labelIid))
  }

  def removeLabelFromContent(contentIid: InternalId, labelIid: InternalId): Unit = {
    val labeledContent = queryLocal[LabeledContent](sql"contentIid = $contentIid and labelIid = $labelIid").head
    delete[LabeledContent](labeledContent.iid)
  }

  def createLabel(parentIid: InternalId, name: String): Label = {
    upsert(Label(name, data = "color" -> "#7F7F7F"), Some(parentIid))
  }

  def updateLabel(iid: InternalId, name: String, data: JValue): Unit = {
    val label = queryLocal[Label](sql"iid = $iid").head
    upsert(label.copy(name = name, data = data))
  }

  def moveLabel(parentIid: InternalId, newParentIid: InternalId, iid: InternalId): Unit = {
    val labelChild = queryLocal[LabelChild](sql"parentIid = $parentIid and childIid = $iid").head
    upsert(LabelChild(newParentIid, iid))
    delete[LabelChild](labelChild.iid)
  }

  def copyLabel(parentIid: InternalId, iid: InternalId): Unit = {
    upsert(LabelChild(parentIid, iid))
  }

  def deleteLabel(parentIid: InternalId, iid: InternalId): Unit = {
    val labelChild = queryLocal[LabelChild](sql"parentIid = $parentIid and childIid = $iid").head
    delete[LabelChild](labelChild.iid)
  }

  def grantAccess(connectionIid: InternalId, labelIid: InternalId): Unit = {
    upsert(LabelAcl(connectionIid, labelIid))
  }

  def revokeAccess(connectionIid: InternalId, labelIid: InternalId): Unit = {
    val labelAcl = queryLocal[LabelAcl](sql"connectionIid = $connectionIid and labelIid = $labelIid").head
    delete[LabelAcl](labelAcl.iid)
  }

  def consumeNotification(iid: InternalId): Unit = {
    val notification = queryLocal[Notification](sql"iid = $iid").head
    upsert(notification.copy(consumed = true))
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
