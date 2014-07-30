package com.qoid.bennu.distributed

import com.qoid.bennu.BennuException
import com.qoid.bennu.ErrorCode
import com.qoid.bennu.JsonAssist._
import m3.TypeInfo
import m3.predef.Logging
import m3.predef.ScalaInjector

trait DistributedRequestHandler[T] extends DistributedHandler with Logging {
  protected val requestKind: DistributedMessageKind
  protected val responseKind: DistributedMessageKind
  protected val allowedVersions: List[Int]

  protected def validateRequest(request: T): Unit = ()
  protected def process(request: T, injector: ScalaInjector): JValue

  override def handle(message: DistributedMessage, injector: ScalaInjector): Unit = {
    val distributedMgr = injector.instance[DistributedManager]

    try {
      if (message.kind != responseKind || !allowedVersions.contains(message.version)) {
        throw new BennuException(ErrorCode.unsupportedMessage, s"${message.kind} v${message.version}")
      }

      val request = serializer.fromJsonTi[T](message.data, TypeInfo(message.data.getClass))

      validateRequest(request)

      val response = process(request, injector)

      val responseMessage = DistributedMessage(responseKind, 1, message.replyRoute, response, Some(message.messageId))

      distributedMgr.sendResponse(responseMessage)
    } catch {
      case e: BennuException =>
        logger.debug(s"BennuException: ${e.getErrorCode()} -- ${e.getMessage}")
        distributedMgr.sendError(e, message)
      case e: Exception =>
        logger.warn(e)
        distributedMgr.sendError(ErrorCode.unexpectedError, e.getMessage, message)
    }
  }
}
