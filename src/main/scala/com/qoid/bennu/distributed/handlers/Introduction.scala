package com.qoid.bennu.distributed.handlers

import com.qoid.bennu.BennuException
import com.qoid.bennu.ErrorCode
import com.qoid.bennu.JsonAssist._
import com.qoid.bennu.JsonAssist.jsondsl._
import com.qoid.bennu.distributed.DistributedHandler
import com.qoid.bennu.distributed.DistributedManager
import com.qoid.bennu.distributed.DistributedMessage
import com.qoid.bennu.distributed.DistributedMessageKind
import com.qoid.bennu.distributed.DistributedRequestHandler
import com.qoid.bennu.distributed.DistributedResponseHandler
import com.qoid.bennu.distributed.messages
import com.qoid.bennu.model.Alias
import com.qoid.bennu.model.Introduction
import com.qoid.bennu.model.Notification
import com.qoid.bennu.model.assist.ConnectionAssist
import com.qoid.bennu.model.id.PeerId
import com.qoid.bennu.model.notification.NotificationKind
import com.qoid.bennu.security.AgentSecurityContext
import com.qoid.bennu.security.ConnectionSecurityContext
import com.qoid.bennu.security.SecurityContext
import m3.predef._

object InitiateIntroductionRequest extends DistributedRequestHandler[messages.InitiateIntroductionRequest] {
  override protected val requestKind = DistributedMessageKind.InitiateIntroductionRequest
  override protected val responseKind = DistributedMessageKind.InitiateIntroductionResponse
  override protected val allowedVersions = List(1)

  override def process(message: DistributedMessage, request: messages.InitiateIntroductionRequest, injector: ScalaInjector): JValue = {
    val distributedMgr = injector.instance[DistributedManager]

    if (request.aMessage.isEmpty) throw new BennuException(ErrorCode.aMessageInvalid)
    if (request.bMessage.isEmpty) throw new BennuException(ErrorCode.bMessageInvalid)

    val introduction = Introduction.insert(Introduction(
      request.aConnectionIid,
      false,
      request.bConnectionIid,
      false
    ))

    // Send IntroductionRequest to A
    val aIntroductionRequest = messages.IntroductionRequest(introduction.iid, request.aMessage, request.bConnectionIid)
    val aIntroductionRequestMessage = DistributedMessage(DistributedMessageKind.IntroductionRequest, 1, List(request.aConnectionIid), aIntroductionRequest.toJson)
    distributedMgr.send(aIntroductionRequestMessage)

    // Send IntroductionRequest to B
    val bIntroductionRequest = messages.IntroductionRequest(introduction.iid, request.bMessage, request.aConnectionIid)
    val bIntroductionRequestMessage = DistributedMessage(DistributedMessageKind.IntroductionRequest, 1, List(request.bConnectionIid), bIntroductionRequest.toJson)
    distributedMgr.send(bIntroductionRequestMessage)

    messages.InitiateIntroductionResponse(introduction.iid).toJson
  }
}

object InitiateIntroductionResponse extends DistributedResponseHandler[messages.InitiateIntroductionResponse] {
  override protected val responseKind = DistributedMessageKind.InitiateIntroductionResponse
  override protected val allowedVersions = List(1)

  override protected def getServiceResult(response: messages.InitiateIntroductionResponse, message: DistributedMessage): JValue = {
    "introductionIid" -> response.introductionIid
  }
}

object IntroductionRequest extends DistributedHandler with Logging {
  private val messageKind: DistributedMessageKind = DistributedMessageKind.IntroductionRequest
  private val allowedVersions: List[Int] = List(1)

  override def handle(message: DistributedMessage, injector: ScalaInjector): Unit = {
    try {
      if (message.kind != messageKind || !allowedVersions.contains(message.version)) {
        throw new BennuException(ErrorCode.unsupportedMessage, s"${message.kind} v${message.version}")
      }

      Notification.insert(Notification(NotificationKind.IntroductionRequest, data = message.data))
    } catch {
      case e: BennuException => logger.debug(s"BennuException: ${e.getErrorCode()} -- ${e.getMessage}")
      case e: Exception => logger.warn(e)
    }
  }
}

object IntroductionResponse extends DistributedHandler with Logging {
  private val messageKind: DistributedMessageKind = DistributedMessageKind.IntroductionResponse
  private val allowedVersions: List[Int] = List(1)

  override def handle(message: DistributedMessage, injector: ScalaInjector): Unit = {
    try {
      if (message.kind != messageKind || !allowedVersions.contains(message.version)) {
        throw new BennuException(ErrorCode.unsupportedMessage, s"${message.kind} v${message.version}")
      }

      val response = serializer.fromJson[messages.IntroductionResponse](message.data)
      val connectionIid = message.replyRoute.head

      val securityContext = injector.instance[SecurityContext]

      // Get the self-connection iid of the alias
      val aliasConnectionIid = AgentSecurityContext(securityContext.agentId) {
        Alias.fetch(securityContext.aliasIid).connectionIid
      }

      ConnectionSecurityContext(aliasConnectionIid, message.replyRoute.size, injector) {
        val introduction = Introduction.fetch(response.introductionIid)

        // Update introduction record
        val updatedIntroduction = connectionIid match {
          case introduction.aConnectionIid => updateWithRetry(introduction, Some(true), None)
          case introduction.bConnectionIid => updateWithRetry(introduction, None, Some(true))
          case _ => m3x.error("IntroductionResponseHandler -- Connection iid doesn't match introduction")
        }

        // Send connect messages if both parties have accepted
        if (updatedIntroduction.aAccepted && updatedIntroduction.bAccepted) {
          sendConnectMessages(updatedIntroduction, injector)
        }
      }
    } catch {
      case e: BennuException => logger.debug(s"BennuException: ${e.getErrorCode()} -- ${e.getMessage}")
      case e: Exception => logger.warn(e)
    }
  }

  private def updateWithRetry(
    introduction: Introduction,
    aAccepted: Option[Boolean],
    bAccepted: Option[Boolean]
  ): Introduction = {
    try {
      Introduction.update(introduction.copy(
        aAccepted = aAccepted.getOrElse(introduction.aAccepted),
        bAccepted = bAccepted.getOrElse(introduction.bAccepted)
      ))
    } catch {
      case _: Exception =>
        val introduction2 = Introduction.fetch(introduction.iid)
        Introduction.update(introduction2.copy(
          aAccepted = aAccepted.getOrElse(introduction2.aAccepted),
          bAccepted = bAccepted.getOrElse(introduction2.bAccepted)
        ))
    }
  }

  private def sendConnectMessages(introduction: Introduction, injector: ScalaInjector): Unit = {
    val distributedMgr = injector.instance[DistributedManager]

    val peerId1 = PeerId.random
    val peerId2 = PeerId.random

    val aIntroductionConnect = messages.IntroductionConnect(peerId1, peerId2)
    val aIntroductionConnectMessage = DistributedMessage(DistributedMessageKind.IntroductionConnect, 1, List(introduction.aConnectionIid), aIntroductionConnect.toJson)
    distributedMgr.send(aIntroductionConnectMessage)

    val bIntroductionConnect = messages.IntroductionConnect(peerId2, peerId1)
    val bIntroductionConnectMessage = DistributedMessage(DistributedMessageKind.IntroductionConnect, 1, List(introduction.bConnectionIid), bIntroductionConnect.toJson)
    distributedMgr.send(bIntroductionConnectMessage)
  }
}

object AcceptIntroductionRequest extends DistributedRequestHandler[messages.AcceptIntroductionRequest] {
  override protected val requestKind = DistributedMessageKind.AcceptIntroductionRequest
  override protected val responseKind = DistributedMessageKind.AcceptIntroductionResponse
  override protected val allowedVersions = List(1)

  override def process(message: DistributedMessage, request: messages.AcceptIntroductionRequest, injector: ScalaInjector): JValue = {
    val distributedMgr = injector.instance[DistributedManager]

    val notification = Notification.fetch(request.notificationIid)

    val introductionRequest = serializer.fromJson[messages.IntroductionRequest](notification.data)

    val introductionResponse = messages.IntroductionResponse(introductionRequest.introductionIid)
    val introductionResponseMessage = DistributedMessage(DistributedMessageKind.IntroductionResponse, 1, List(notification.createdByConnectionIid), introductionResponse.toJson)
    distributedMgr.send(introductionResponseMessage)

    messages.AcceptIntroductionResponse(request.notificationIid).toJson
  }
}

object AcceptIntroductionResponse extends DistributedResponseHandler[messages.AcceptIntroductionResponse] {
  override protected val responseKind = DistributedMessageKind.AcceptIntroductionResponse
  override protected val allowedVersions = List(1)

  override protected def getServiceResult(response: messages.AcceptIntroductionResponse, message: DistributedMessage): JValue = {
    "notificationIid" -> response.notificationIid
  }
}

object IntroductionConnect extends DistributedHandler with Logging {
  private val messageKind: DistributedMessageKind = DistributedMessageKind.IntroductionConnect
  private val allowedVersions: List[Int] = List(1)

  override def handle(message: DistributedMessage, injector: ScalaInjector): Unit = {
    try {
      if (message.kind != messageKind || !allowedVersions.contains(message.version)) {
        throw new BennuException(ErrorCode.unsupportedMessage, s"${message.kind} v${message.version}")
      }

      val connectionAssist = injector.instance[ConnectionAssist]

      val introductionConnect = serializer.fromJson[messages.IntroductionConnect](message.data)

      val securityContext = injector.instance[SecurityContext]

      // Get the self-connection iid of the alias
      val aliasConnectionIid = AgentSecurityContext(securityContext.agentId) {
        Alias.fetch(securityContext.aliasIid).connectionIid
      }

      ConnectionSecurityContext(aliasConnectionIid, message.replyRoute.size, injector) {
        connectionAssist.createConnection(introductionConnect.localPeerId, introductionConnect.remotePeerId)
      }
    } catch {
      case e: BennuException => logger.debug(s"BennuException: ${e.getErrorCode()} -- ${e.getMessage}")
      case e: Exception => logger.warn(e)
    }
  }
}
