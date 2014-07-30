package com.qoid.bennu.distributed.handlers

import com.qoid.bennu.BennuException
import com.qoid.bennu.ErrorCode
import com.qoid.bennu.JsonAssist.serializer
import com.qoid.bennu.distributed.DistributedHandler
import com.qoid.bennu.distributed.DistributedManager
import com.qoid.bennu.distributed.DistributedMessage
import com.qoid.bennu.distributed.DistributedMessageKind
import com.qoid.bennu.distributed.messages
import m3.TypeInfo
import m3.predef._

object Error extends DistributedHandler with Logging {
  private val responseKind = DistributedMessageKind.Error
  private val allowedVersions = List(1)

  def handle(message: DistributedMessage, injector: ScalaInjector): Unit = {
    val distributedMgr = injector.instance[DistributedManager]

    message.replyToMessageId match {
      case Some(replyToMessageId) =>
        try {
          if (message.kind != responseKind || !allowedVersions.contains(message.version)) {
            throw new BennuException(ErrorCode.unsupportedResponseMessage, s"${message.kind} v${message.version}")
          }

          val error = serializer.fromJsonTi[messages.Error](message.data, TypeInfo(message.data.getClass))

          logger.warn(s"Error message -- ${error.errorCode} -- ${error.message}")

          distributedMgr.putErrorOnChannel(message.messageId, error.errorCode)
        } catch {
          case e: BennuException =>
            logger.debug(s"BennuException: ${e.getErrorCode()} -- ${e.getMessage}")
            distributedMgr.putErrorOnChannel(replyToMessageId, e.getErrorCode())
          case e: Exception =>
            logger.warn(e)
            distributedMgr.putErrorOnChannel(replyToMessageId, ErrorCode.unexpectedError)
        }

      case None => logger.warn(s"ReplyToMessageId not included in response")
    }
  }
}
