package com.qoid.bennu.distributed.messages

import com.qoid.bennu.ErrorCode
import com.qoid.bennu.FromJsonCapable
import com.qoid.bennu.ToJsonCapable
import com.qoid.bennu.distributed.DistributedHandler
import com.qoid.bennu.distributed.DistributedManager
import com.qoid.bennu.distributed.DistributedMessage
import com.qoid.bennu.distributed.DistributedMessageKind
import m3.predef._

object Error extends DistributedHandler with FromJsonCapable[Error] with Logging {
  def handle(message: DistributedMessage, injector: ScalaInjector): Unit = {
    val distributedMgr = injector.instance[DistributedManager]

    (message.kind, message.version) match {
      case (DistributedMessageKind.Error, 1) =>
        try {
          // Deserialize message data
          val error = fromJson(message.data)

          // Log error
          logger.warn(s"Error message -- ${error.errorCode} -- ${error.message}")

          // Put response on channel
          distributedMgr.putErrorOnChannel(message.messageId, error.errorCode)
        } catch {
          case e: Exception =>
            logger.warn(e)
            distributedMgr.putErrorOnChannel(message.messageId, ErrorCode.unexpectedError)
        }

      case (kind, version) =>
        logger.warn(s"Unsupported message -- ${kind} v${version}")
        distributedMgr.putErrorOnChannel(message.messageId, ErrorCode.unsupportedResponseMessage)
    }
  }
}

case class Error(
  errorCode: String,
  message: String
) extends ToJsonCapable
