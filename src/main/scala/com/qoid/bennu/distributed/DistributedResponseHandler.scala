package com.qoid.bennu.distributed

import com.qoid.bennu.BennuException
import com.qoid.bennu.ErrorCode
import com.qoid.bennu.JsonAssist._
import m3.TypeInfo
import m3.predef._

trait DistributedResponseHandler[T] extends DistributedHandler with Logging {
  protected val responseKind: DistributedMessageKind
  protected val allowedVersions: List[Int]

  override def handle(message: DistributedMessage, injector: ScalaInjector): Unit = {
    val distributedMgr = injector.instance[DistributedManager]

    message.replyToMessageId match {
      case Some(replyToMessageId) =>
        try {
          if (message.kind != responseKind || !allowedVersions.contains(message.version)) {
            throw new BennuException(ErrorCode.unsupportedResponseMessage, s"${message.kind} v${message.version}")
          }

          serializer.fromJsonTi[T](message.data, TypeInfo(message.data.getClass))

          val result = DistributedResult(responseKind, message.data)
          distributedMgr.putResponseOnChannel(replyToMessageId, result)
        } catch {
          case e: BennuException =>
            logger.debug(s"BennuException: ${e.getErrorCode()} -- ${e.getMessage}")
            distributedMgr.putErrorOnChannel(replyToMessageId, e.getErrorCode())
          case e: Exception =>
            logger.warn(e)
            distributedMgr.putErrorOnChannel(replyToMessageId, ErrorCode.unexpectedError)
        }

      case None =>
        logger.warn(s"ReplyToMessageId not included in response")
    }
  }
}
