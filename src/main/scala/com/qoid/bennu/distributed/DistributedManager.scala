package com.qoid.bennu.distributed

import com.google.inject.Inject
import com.google.inject.Singleton
import com.qoid.bennu.JsonAssist._
import com.qoid.bennu.distributed.messages._
import com.qoid.bennu.model.Connection
import com.qoid.bennu.model.id.InternalId
import com.qoid.bennu.security.ConnectionSecurityContext
import com.qoid.bennu.security.SystemSecurityContext
import m3.predef._

@Singleton
class DistributedManager @Inject()(
  injector: ScalaInjector,
  messageQueue: MessageQueue
) extends Logging {

  def initialize(): Unit = {
    val connections = SystemSecurityContext {
      Connection.selectAll.toList
    }

    listen(connections)
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

  def send(message: DistributedMessage): Unit = {
    if (!sendMessage(message)) {
      logger.warn("no connection iids in message")
    }
  }

  private def messageHandler(connectionIid: InternalId)(message: DistributedMessage): Unit = {
    val message2 = message.copy(replyRoute = connectionIid :: message.replyRoute)

    SystemSecurityContext {
      val connection = Connection.fetch(connectionIid)

      logger.debug(
        s"received message (${connection.remotePeerId.value} -> ${connection.localPeerId.value}):" +
          message2.toJson.toJsonStr
      )
    }

    if (!sendMessage(message2)) {
      ConnectionSecurityContext(connectionIid, injector) {
        message2.kind.handle(message2, injector)
      }
    }
  }

  private def sendMessage(message: DistributedMessage): Boolean = {
    message.route match {
      case connectionIid :: connectionIids =>
        SystemSecurityContext {
          val connection = Connection.fetch(connectionIid)

          logger.debug(
            s"sending message (${connection.localPeerId.value} -> ${connection.remotePeerId.value}):" +
              message.toJson.toJsonStr
          )

          messageQueue.enqueue(connection, message.copy(route = connectionIids))
        }

        true

      case _ => false
    }
  }
}
