package com.qoid.bennu.distributed

import com.google.inject.Inject
import com.google.inject.Singleton
import com.qoid.bennu.BennuException
import com.qoid.bennu.JsonAssist._
import com.qoid.bennu.distributed.messages._
import com.qoid.bennu.model.Connection
import com.qoid.bennu.model.id.DistributedMessageId
import com.qoid.bennu.model.id.InternalId
import com.qoid.bennu.security.ConnectionSecurityContext
import com.qoid.bennu.security.SecurityContext
import com.qoid.bennu.security.SystemSecurityContext
import com.qoid.bennu.session.SessionManager
import m3.LockFreeMap
import m3.predef._
import m3.servlet.beans.MultiRequestHandler.MethodInvocationError
import m3.servlet.beans.MultiRequestHandler.MethodInvocationResult

@Singleton
class DistributedManager @Inject()(
  injector: ScalaInjector,
  messageQueue: MessageQueue,
  sessionMgr: SessionManager
) extends Logging {

  //TODO: If there is a problem forwarding message or handling message or any security problems, send a response back to message originator

  private val requestDataStore = LockFreeMap.empty[(InternalId, DistributedMessageId), RequestData]

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

  def send(message: DistributedMessage, requestData: RequestData): Unit = {
    send(message, Some(requestData))
  }

  def send(message: DistributedMessage, requestDataOpt: Option[RequestData] = None): Unit = {
    message.route match {
      case connectionIid :: connectionIids =>
        requestDataOpt.foreach(requestData => storeRequestData(connectionIid, message.messageId, requestData))
        sendMessage(connectionIid, message.copy(route = connectionIids))

      case _ => logger.warn("no connection iids in message")
    }
  }

  def sendError(e: BennuException, message: DistributedMessage): Unit = {
    sendError(e.getErrorCode(), e.getMessage, message)
  }

  def sendError(errorCode: String, errorDetails: String, message: DistributedMessage): Unit = {
    val error = Error(errorCode, errorDetails)
    val errorMessage = DistributedMessage(DistributedMessageKind.Error, 1, message.replyRoute, error.toJson, Some(message.messageId))
    send(errorMessage)
  }

  def putResponseOnChannel(messageId: DistributedMessageId, result: JValue): Unit = {
    val securityContext = injector.instance[SecurityContext]

    requestDataStore.get((securityContext.connectionIid, messageId)).foreach { requestData =>
      sessionMgr.getSessionOpt(requestData.channelId) match {
        case Some(session) => session.put(MethodInvocationResult(true, requestData.context, result, None))
        case None => requestDataStore.remove((securityContext.connectionIid, messageId))
      }

      if (requestData.singleResponse) {
        requestDataStore.remove((securityContext.connectionIid, messageId))
      }
    }
  }

  def putErrorOnChannel(messageId: DistributedMessageId, errorCode: String): Unit = {
    val securityContext = injector.instance[SecurityContext]

    requestDataStore.get((securityContext.connectionIid, messageId)).foreach { requestData =>
      sessionMgr.getSessionOpt(requestData.channelId).foreach { session =>
        session.put(MethodInvocationResult(false, requestData.context, JNothing, Some(MethodInvocationError(errorCode, ""))))
      }

      if (requestData.singleResponse) {
        requestDataStore.remove((securityContext.connectionIid, messageId))
      }
    }
  }

  def removeRequestData(messageId: DistributedMessageId): Unit = {
    val securityContext = injector.instance[SecurityContext]
    requestDataStore.remove((securityContext.connectionIid, messageId))
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

    message.route match {
      case toConnectionIid :: toConnectionIids =>
        sendMessage(toConnectionIid, message2.copy(route = toConnectionIids))
      case _ =>
        ConnectionSecurityContext(connectionIid, message2.replyRoute.size, injector) {
          message2.kind.handler.handle(message2, injector)
        }
    }
  }

  private def storeRequestData(
    connectionIid: InternalId,
    messageId: DistributedMessageId,
    requestData: RequestData
  ): Unit = {
    requestDataStore.put((connectionIid, messageId), requestData)
  }

  private def sendMessage(connectionIid: InternalId, message: DistributedMessage): Unit = {
    SystemSecurityContext {
      val connection = Connection.fetch(connectionIid)

      logger.debug(
        s"sending message (${connection.localPeerId.value} -> ${connection.remotePeerId.value}):" +
          message.toJson.toJsonStr
      )

      messageQueue.enqueue(connection, message)
    }
  }
}
