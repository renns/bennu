package com.qoid.bennu.testclient.client

import com.qoid.bennu.model._
import m3.json.LiftJsonAssist._
import scala.concurrent._

trait ModelAssist {
  this: ChannelClient with ServiceAssist =>

  def createAlias(rootLabelIid: InternalId, name: String)(implicit ec: ExecutionContext): Future[Alias] = {
    upsert(Alias(InternalId.random, agentId, rootLabelIid, name, JNothing))
  }

  def createConnection(aliasId: InternalId, localPeerId: PeerId, remotePeerId: PeerId)(implicit ec: ExecutionContext): Future[Connection] = {
    upsert(Connection(InternalId.random, agentId, aliasId, localPeerId, remotePeerId, JNothing))
  }

  def createLabel(name: String)(implicit ec: ExecutionContext): Future[Label] = {
    upsert(Label(InternalId.random, agentId, name, JNothing))
  }

  def createLabelChild(parentIid: InternalId, childIid: InternalId)(implicit ec: ExecutionContext): Future[LabelChild] = {
    upsert(LabelChild(InternalId.random, agentId, parentIid, childIid, JNothing))
  }
}
