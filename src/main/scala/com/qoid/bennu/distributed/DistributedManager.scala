package com.qoid.bennu.distributed

import com.google.inject.Inject
import com.google.inject.Singleton
import com.qoid.bennu.JsonAssist._
import com.qoid.bennu.distributed.handlers._
import com.qoid.bennu.distributed.messages._
import com.qoid.bennu.model.Connection
import com.qoid.bennu.model.id.InternalId
import com.qoid.bennu.security.AgentView
import com.qoid.bennu.security.ConnectionSecurityContext
import com.qoid.bennu.security.SecurityContext
import java.sql.{ Connection => JdbcConn }
import m3.Txn
import m3.predef._

@Singleton
class DistributedManager @Inject()(
  injector: ScalaInjector,
  messageQueue: MessageQueue
) extends Logging {

  def initialize(implicit jdbcConn: JdbcConn): Unit = {
    listen(Connection.select("1 = 1").toList)
  }

  def listen(connection: Connection): Unit = listen(List(connection))

  def listen(connections: List[Connection]): Unit = {
    logger.debug(
      "listening on connections:\n" +
        connections.map(c => s"\t${c.localPeerId.value} -> ${c.remotePeerId.value}").mkString("\n")
    )

    messageQueue.subscribe(connections, messageHandler)
  }

  def stopListen(connection: Connection): Unit = {
    logger.debug(
      "stop listening on connection:\n" +
        s"\t${connection.localPeerId.value} -> ${connection.remotePeerId.value}"
    )

    messageQueue.unsubscribe(connection)
  }

  def send(connection: Connection, message: DistributedMessage): Unit = {
    import scala.concurrent.ExecutionContext.Implicits.global
    import scala.concurrent.future

    future {
      logger.debug(
        s"sending message (${connection.localPeerId.value} -> ${connection.remotePeerId.value}}):" +
          message.toJson.toJsonStr
      )

      try {
        messageQueue.enqueue(connection, message)
      } catch {
        case e: Exception => logger.warn(e)
      }
    }
  }

  def messageHandler(connectionIid: InternalId)(message: DistributedMessage): Unit = {
    Txn {
      Txn.setViaTypename[SecurityContext](ConnectionSecurityContext(injector, connectionIid))

      val av = injector.instance[AgentView]
      val connection = av.fetch[Connection](connectionIid)

      logger.debug(
        s"received message (${connection.localPeerId.value} <- ${connection.remotePeerId.value}}):" +
          message.toJson.toJsonStr
      )

      (message.kind, message.version) match {
        case (DistributedMessageKind.QueryRequest, 1) =>
          QueryRequestHandler.handle(connection, QueryRequest.fromJson(message.data), injector)
        case (DistributedMessageKind.QueryResponse, 1) =>
          QueryResponseHandler.handle(connection, QueryResponse.fromJson(message.data), injector)
        case (DistributedMessageKind.DeRegisterStandingQuery, 1) =>
          DeRegisterStandingQueryHandler.handle(connection, DeRegisterStandingQuery.fromJson(message.data), injector)
        case (DistributedMessageKind.IntroductionRequest, 1) =>
          IntroductionRequestHandler.handle(connection, IntroductionRequest.fromJson(message.data), injector)
        case (DistributedMessageKind.IntroductionResponse, 1) =>
          IntroductionResponseHandler.handle(connection, IntroductionResponse.fromJson(message.data), injector)
        case (DistributedMessageKind.IntroductionConnect, 1) =>
          IntroductionConnectHandler.handle(connection, IntroductionConnect.fromJson(message.data), injector)
        case (DistributedMessageKind.VerificationRequest, 1) =>
          VerificationRequestHandler.handle(connection, VerificationRequest.fromJson(message.data), injector)
        case (DistributedMessageKind.VerificationResponse, 1) =>
          VerificationResponseHandler.handle(connection, VerificationResponse.fromJson(message.data), injector)
        case _ =>
          logger.warn(s"unhandled distributed message -- ${message.kind},${message.version}")
      }
    }
  }
}
