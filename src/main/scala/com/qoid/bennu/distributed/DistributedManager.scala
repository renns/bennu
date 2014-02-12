package com.qoid.bennu.distributed

import com.qoid.bennu.JsonAssist._
import com.qoid.bennu.model._
import com.qoid.bennu.squery.StandingQueryAction
import java.sql.{ Connection => JdbcConn }
import m3.jdbc._
import m3.predef._
import com.qoid.bennu.Enum

@com.google.inject.Singleton
class DistributedManager extends Logging {
//
//  def sendRequest(
//    connectionIid: InternalId,
//    kind: DistributedRequestKind,
//    data: JValue
//  )(
//    implicit
//    jdbcConn: JdbcConn
//  ): Unit = {
//    val connection = Connection.fetch(connectionIid)
//
//    val request = DistributedRequest(
//      connection.localPeerId,
//      connection.remotePeerId,
//      kind,
//      data
//    )
//
//    getConnection(request.toPeerId, request.fromPeerId) match {
//      case Some(toConnection) =>
//        // The to-connection exists locally
//        getRequestHandler(kind).foreach { case handler => handler.handle() }
//      case None =>
//        // The to-connection doesn't exist locally
//        // TODO: Send request on distributed network
//        ()
//    }
//  }
//
//  private def getRequestHandler(kind: DistributedRequestKind): Option[DistributionRequestHandler] = {
//    kind match {
//      case DistributedRequestKind.GetProfile => Some(new GetProfileHandler)
//      case k =>
//        logger.warn("DistributedRequestKind not recognized -- " + k)
//        None
//    }
//  }
//
//  private def getConnection(
//    localPeerId: PeerId,
//    remotePeerId: PeerId
//  )(
//    implicit
//    jdbcConn: JdbcConn
//  ): Option[Connection] = {
//    val sql = sql"""localPeerId = '$localPeerId' and remotePeerId = '$remotePeerId'"""
//    Connection.selectOpt(sql)
//  }

  def sendNotification(
    connectionIid: InternalId,
    kind: NotificationKind,
    data: JValue
  )(
    implicit
    jdbcConn: JdbcConn
  ): Unit = {

    val connection = Connection.fetch(connectionIid)
    val remoteConnection = Connection.selectBox(sql"localPeerId = ${connection.remotePeerId}").open_$

    val notification = Notification(
      agentId = remoteConnection.agentId,
      consumed = false,
      fromConnectionIid = remoteConnection.iid,
      kind = kind,
      data = data
    )

    notification.sqlInsert.notifyStandingQueries(StandingQueryAction.Insert)
    inject[NotificationListener].fireNotification(notification)
  }

  def getProfile(connectionIid: InternalId)(implicit jdbcConn: JdbcConn): JValue = {
    val connection = Connection.fetch(connectionIid)
    val remoteConnection = Connection.selectBox(sql"localPeerId = ${connection.remotePeerId}").open_$
    val remoteAlias = Alias.fetch(remoteConnection.aliasIid)
    remoteAlias.profile
  }
}
//
//case class DistributedRequest(
//  fromPeerId: PeerId,
//  toPeerId: PeerId,
//  kind: DistributedRequestKind,
//  data: JValue,
//  iid: InternalId = InternalId.random
//)
//
//sealed trait DistributedRequestKind
//
//object DistributedRequestKind extends Enum[DistributedRequestKind] {
//  case object GetProfile extends DistributedRequestKind
//
//  override val values: Set[DistributedRequestKind] = Set(
//    GetProfile
//  )
//}
//
//trait DistributionRequestHandler {
//  def handle(): Unit
//}
//
//class GetProfileHandler extends DistributionRequestHandler {
//  def handle(): Unit = {
//
//  }
//}
