package com.qoid.bennu.distributed

import com.qoid.bennu.BennuException
import com.qoid.bennu.ErrorCode
import com.qoid.bennu.JsonAssist._
import com.qoid.bennu.model.Content
import com.qoid.bennu.model.LabeledContent
import com.qoid.bennu.model.assist.LabelAssist
import com.qoid.bennu.model.content.AuditLog
import com.qoid.bennu.security.AgentSecurityContext
import com.qoid.bennu.security.SecurityContext
import m3.predef.Logging
import m3.predef.ScalaInjector

abstract class DistributedRequestHandler[T : Manifest] extends DistributedHandler with Logging {
  protected val requestKind: DistributedMessageKind
  protected val responseKind: DistributedMessageKind
  protected val allowedVersions: List[Int]

  protected def process(message: DistributedMessage, request: T, injector: ScalaInjector): JValue

  override def handle(message: DistributedMessage, injector: ScalaInjector): Unit = {
    val distributedMgr = injector.instance[DistributedManager]

    try {
      if (message.kind != requestKind || !allowedVersions.contains(message.version)) {
        throw new BennuException(ErrorCode.unsupportedMessage, s"${message.kind} v${message.version}")
      }

      val request = serializer.fromJson[T](message.data)

      val response = process(message, request, injector)

      val responseMessage = DistributedMessage(responseKind, 1, message.replyRoute, response, Some(message.messageId))

      distributedMgr.send(responseMessage)

      createAuditLog(injector, message)
    } catch {
      case e: BennuException =>
        logger.debug(s"BennuException: ${e.getErrorCode()} -- ${e.getMessage}")
        distributedMgr.sendError(e, message)
        createAuditLog(injector, message, false, Some(e.getErrorCode()))
      case e: Exception =>
        logger.warn(e)
        distributedMgr.sendError(ErrorCode.unexpectedError, e.getMessage, message)
        createAuditLog(injector, message, false, Some(ErrorCode.unexpectedError))
    }
  }

  private def createAuditLog(
    injector: ScalaInjector,
    message: DistributedMessage,
    success: Boolean = true,
    errorCode: Option[String] = None
  ): Unit = {
    try {
      val labelAssist = injector.instance[LabelAssist]

      labelAssist.resolveAuditLogMetaLabel().foreach { labelIid =>
        AgentSecurityContext(injector.instance[SecurityContext].agentId) {
          val auditLog = AuditLog(message.kind, message.replyRoute, message.data, success, errorCode)
          val content = Content.insert(Content("AUDIT_LOG", None, data = auditLog.toJson))
          LabeledContent.insert(LabeledContent(content.iid, labelIid))
        }
      }
    } catch {
      case e: Exception => logger.warn(e)
    }
  }
}
