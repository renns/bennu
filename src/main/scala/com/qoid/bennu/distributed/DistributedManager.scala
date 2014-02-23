package com.qoid.bennu.distributed

import com.qoid.bennu.JsonAssist._
import com.qoid.bennu.SecurityContext
import com.qoid.bennu.SecurityContext.ConnectionSecurityContext
import com.qoid.bennu.SecurityContext.AliasSecurityContext
import com.qoid.bennu.model._
import com.qoid.bennu.util.Implicits.futureExtensions
import java.sql.{ Connection => JdbcConn }
import m3.Txn
import m3.jdbc._
import m3.predef._
import net.model3.lang.TimeDuration
import scala.Some
import scala.concurrent._

@com.google.inject.Singleton
class DistributedManager {

  /**
   * send a distributed request to a connection 
   */
  def sendRequest(
    connectionIid: InternalId,
    kind: DistributedRequestKind,
    data: JValue,
    timeout: TimeDuration = new TimeDuration(0)
  )(
    implicit
    ec: ExecutionContext
  ): Future[JValue] = {
    val p = Promise[JValue]()

    future {
      Txn {
        implicit val jdbcConn = inject[JdbcConn]

        val connection = Connection.fetch(connectionIid)
        val request = DistributedRequest(
          connection.localPeerId,
          connection.remotePeerId,
          kind,
          data
        )

        val toConnectionSql = sql"localPeerId = ${request.toPeerId} and remotePeerId = ${request.fromPeerId}"
        val toConnectionOpt = Connection.selectOpt(toConnectionSql)

        toConnectionOpt match {
          case Some(toConnection) =>
                    
            // The to-connection exists locally
            val sc = ConnectionSecurityContext(toConnection.iid)
            Txn.setViaTypename[SecurityContext](sc)

            DistributedRequestHandler.getHandler(kind) match {
              case Some(handler) =>
                val response = handler.handle(request, toConnection)
                p.success(response.data)
              case None => p.failure(new Exception(s"No handler found for distributed request kind -- $kind"))
            }
          case _ =>
            // The to-connection doesn't exist locally
            // TODO: Send request on distributed network
            // TODO: Remove failure below once we're distributed
            p.failure(new Exception(s"Remote connection not found in node. Since we're not distributed, it should exist."))
        }
      }
    }.onFailure { case t =>
      p.failure(t)
    }

    p.future.withTimeout(timeout)
  }


  def sendNotification(
    connectionIid: InternalId,
    kind: NotificationKind,
    data: JValue
  )(
    implicit
    ec: ExecutionContext
  ): Future[JValue] = {

    val notificationRequest = NotificationHandler.Request(
      kind = kind,
      data = data
    )

    sendRequest(connectionIid, DistributedRequestKind.Notification, notificationRequest.toJson)
  }
}
