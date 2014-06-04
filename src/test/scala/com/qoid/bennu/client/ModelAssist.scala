package com.qoid.bennu.client

import com.qoid.bennu.JsonAssist._
import com.qoid.bennu.JsonAssist.jsondsl._
import com.qoid.bennu.model._
import com.qoid.bennu.model.id._
import m3.jdbc._
import m3.predef._
import scala.async.Async._
import scala.concurrent.{Promise, Future}
import com.qoid.bennu.model.notification.{IntroductionRequest, NotificationKind}
import com.qoid.bennu.squery.StandingQueryAction

trait ModelAssist {
  this: ChannelClient with ServiceAssist =>

  def createAlias(
    parentIid: InternalId,
    name: String,
    profileName: Option[String] = None,
    profileImgSrc: Option[String] = None
  ): Future[Alias] = {
    upsert(Alias(name), Some(parentIid), profileName, profileImgSrc)
  }

  def updateAlias(iid: InternalId, name: String): Future[Alias] = {
    async {
      val alias = await(queryLocal[Alias](sql"iid = $iid")).head
      await(upsert(alias.copy(name = name)))
    }
  }

  def deleteAlias(iid: InternalId): Future[Unit] = {
    delete[Alias](iid).map(_ => ())
  }

  def updateProfile(aliasIid: InternalId, name: String, imgSrc: String): Future[Profile] = {
    async {
      val profile = await(queryLocal[Profile](sql"aliasIid = $aliasIid")).head
      await(upsert(profile.copy(name = name, imgSrc = imgSrc)))
    }
  }

  def updateConnection(iid: InternalId, allowedDegreesOfVisibility: Int): Future[Connection] = {
    async {
      val connection = await(queryLocal[Connection](sql"iid = $iid")).head
      await(upsert(connection.copy(allowedDegreesOfVisibility = allowedDegreesOfVisibility)))
    }
  }

  def createContent(
    aliasIid: InternalId,
    contentType: String,
    data: JValue,
    labelIids: List[InternalId] = Nil
  ): Future[Content] = {
    upsert(Content(aliasIid, contentType, data = data), labelIids = labelIids)
  }

  def updateContent(iid: InternalId, data: JValue): Future[Content] = {
    async {
      val content = await(queryLocal[Content](sql"iid = $iid")).head
      await(upsert(content.copy(data = data)))
    }
  }

  def addLabelToContent(contentIid: InternalId, labelIid: InternalId): Future[Unit] = {
    upsert(LabeledContent(contentIid, labelIid)).map(_ => ())
  }

  def removeLabelFromContent(contentIid: InternalId, labelIid: InternalId): Future[Unit] = {
    async {
      val labeledContent = await(queryLocal[LabeledContent](sql"contentIid = $contentIid and labelIid = $labelIid")).head
      await(delete[LabeledContent](labeledContent.iid))
    }
  }

  def createLabel(parentIid: InternalId, name: String): Future[Label] = {
    upsert(Label(name, data = "color" -> "#7F7F7F"), Some(parentIid))
  }

  def updateLabel(iid: InternalId, name: String, data: JValue): Future[Label] = {
    async {
      val label = await(queryLocal[Label](sql"iid = $iid")).head
      await(upsert(label.copy(name = name, data = data)))
    }
  }

  def moveLabel(parentIid: InternalId, newParentIid: InternalId, iid: InternalId): Future[Unit] = {
    async {
      val labelChild = await(queryLocal[LabelChild](sql"parentIid = $parentIid and childIid = $iid")).head
      await(upsert(LabelChild(newParentIid, iid)))
      await(delete[LabelChild](labelChild.iid))
    }
  }

  def copyLabel(parentIid: InternalId, iid: InternalId): Future[Unit] = {
    upsert(LabelChild(parentIid, iid)).map(_ => ())
  }

  def deleteLabel(parentIid: InternalId, iid: InternalId): Future[Unit] = {
    async {
      val labelChild = await(queryLocal[LabelChild](sql"parentIid = $parentIid and childIid = $iid")).head
      await(delete[LabelChild](labelChild.iid))
    }
  }

  def grantAccess(connectionIid: InternalId, labelIid: InternalId): Future[Unit] = {
    upsert(LabelAcl(connectionIid, labelIid)).map(_ => ())
  }

  def revokeAccess(connectionIid: InternalId, labelIid: InternalId): Future[Unit] = {
    async {
      val labelAcl = await(queryLocal[LabelAcl](sql"connectionIid = $connectionIid and labelIid = $labelIid")).head
      await(delete[LabelAcl](labelAcl.iid))
    }
  }

  def consumeNotification(iid: InternalId): Future[Unit] = {
    async {
      val notification = await(queryLocal[Notification](sql"iid = $iid")).head
      await(upsert(notification.copy(consumed = true)))
    }
  }

  def autoAcceptIntroductions(): Future[Unit] = {
    queryLocal[Notification]("", None, Some({ (notification, action, _) =>
      if (notification.kind == NotificationKind.IntroductionRequest && action == StandingQueryAction.Insert) {
        respondToIntroduction(notification, true)
      }
      ()
    })).map(_ => ())
  }

  def getAgent(): Future[Agent] = {
    async {
      await(queryLocal[Agent]("")).head
    }
  }

  def getRootLabel(): Future[Label] = {
    async {
      val alias = await(getRootAlias())
      await(queryLocal[Label](sql"iid = ${alias.rootLabelIid}")).head
    }
  }

  def getRootAlias(): Future[Alias] = {
    async {
      await(queryLocal[Alias](sql"iid = $rootAliasIid")).head
    }
  }

  def getProfile(aliasIid: InternalId): Future[Profile] = {
    async {
      await(queryLocal[Profile](sql"aliasIid = $aliasIid")).head
    }
  }

  def getIntroducerConnection(): Future[Connection] = {
    async {
      val connections = await(queryLocal[Connection](""))

      await(Future.sequence(connections.map { c =>
        async {
          (c, await(queryRemote[Profile]("name = 'Introducer'", List(c.iid))).nonEmpty)
        }
      })).find(_._2).getOrError("Unable to find introducer connection")._1
    }
  }
}
