package com.qoid.bennu.distributed

import com.google.inject.Inject
import com.qoid.bennu.JsonAssist._
import com.qoid.bennu.SecurityContext
import com.qoid.bennu.SecurityContext.ConnectionSecurityContext
import com.qoid.bennu.distributed.handlers._
import com.qoid.bennu.distributed.messages._
import com.qoid.bennu.model._
import java.sql.{ Connection => JdbcConn }
import m3.Txn
import m3.predef._
import net.codingwell.scalaguice.InjectorExtensions.ScalaInjector

@com.google.inject.Singleton
class DistributedManager @Inject()(
  injector: ScalaInjector,
  messageQueue: SimpleMessageQueue
) extends Logging {

  def initialize(implicit jdbcConn: JdbcConn): Unit = {
    listen(Connection.selectAll.toList)
  }

  def listen(connections: List[Connection]): Unit = {
    logger.debug(
      "listening on connections:\n"
        + connections.map(c => s"\t${c.localPeerId.value} -> ${c.remotePeerId.value}").mkString("\n")
    )

    messageQueue.subscribe(connections, messageHandler)
  }

  def send(connection: Connection, message: DistributedMessage): Unit = {
    import scala.concurrent.future
    import scala.concurrent.ExecutionContext.Implicits.global

    future {
      logger.debug(
        s"sending message (${connection.localPeerId.value} -> ${connection.remotePeerId.value}}):"
          + message.toJson.toJsonStr
      )

      messageQueue.enqueue(connection, message)
    }
  }

  def messageHandler(connection: Connection)(message: DistributedMessage): Unit = {
    logger.debug(
      s"received message (${connection.localPeerId.value} <- ${connection.remotePeerId.value}}):"
        + message.toJson.toJsonStr
    )

    Txn {
      Txn.setViaTypename[SecurityContext](ConnectionSecurityContext(connection.iid))

      (message.kind, message.version) match {
        case (DistributedMessageKind.QueryRequest, 1) =>
          QueryRequestHandler.handle(connection, QueryRequest.fromJson(message.data), injector)
        case (DistributedMessageKind.QueryResponse, 1) =>
          QueryResponseHandler.handle(connection, QueryResponse.fromJson(message.data), injector)
        case _ =>
          logger.warn(s"unhandled distributed message -- ${message.kind},${message.version}")
      }
    }
  }


//  /**
//   * send a distributed request to a connection
//   */
//  def sendRequest(
//    connectionIid: InternalId,
//    kind: DistributedRequestKind,
//    data: JValue,
//    timeout: TimeDuration = new TimeDuration(0)
//  )(
//    implicit
//    ec: ExecutionContext
//  ): Future[JValue] = {
//    val p = Promise[JValue]()
//
//    future {
//      Txn {
//        implicit val jdbcConn = inject[JdbcConn]
//
//        val connection = Connection.fetch(connectionIid)
//        val request = DistributedRequest(
//          connection.localPeerId,
//          connection.remotePeerId,
//          kind,
//          data
//        )
//
//        val toConnectionSql = sql"localPeerId = ${request.toPeerId} and remotePeerId = ${request.fromPeerId}"
//        val toConnectionOpt = Connection.selectOpt(toConnectionSql)
//
//        toConnectionOpt match {
//          case Some(toConnection) =>
//
//            // The to-connection exists locally
//            val sc = ConnectionSecurityContext(toConnection.iid)
//            Txn.setViaTypename[SecurityContext](sc)
//
//            DistributedRequestHandler.getHandler(kind) match {
//              case Some(handler) =>
//                val response = handler.handle(request, toConnection)
//                p.success(response.data)
//              case None => p.failure(new Exception(s"No handler found for distributed request kind -- $kind"))
//            }
//          case _ =>
//            // The to-connection doesn't exist locally
//            // TODO: Send request on distributed network
//            // TODO: Remove failure below once we're distributed
//            p.failure(new Exception(s"Remote connection not found in node. Since we're not distributed, it should exist."))
//        }
//      }
//    }.onFailure { case t =>
//      p.failure(t)
//    }
//
//    p.future.withTimeout(timeout)
//  }
//
//
//  def sendNotification(
//    connectionIid: InternalId,
//    kind: NotificationKind,
//    data: JValue
//  )(
//    implicit
//    ec: ExecutionContext
//  ): Future[JValue] = {
//
//    val notificationRequest = NotificationHandler.Request(
//      kind = kind,
//      data = data
//    )
//
//    sendRequest(connectionIid, DistributedRequestKind.Notification, notificationRequest.toJson)
//  }
}
