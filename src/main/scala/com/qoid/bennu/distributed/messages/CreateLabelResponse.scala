package com.qoid.bennu.distributed.messages

import com.qoid.bennu.ErrorCode
import com.qoid.bennu.FromJsonCapable
import com.qoid.bennu.ToJsonCapable
import com.qoid.bennu.distributed.DistributedManager
import com.qoid.bennu.distributed.DistributedResult
import com.qoid.bennu.model.Label
import m3.predef._

object CreateLabelResponse extends FromJsonCapable[CreateLabelResponse] with Logging {
  def handle(message: DistributedMessage, injector: ScalaInjector): Unit = {
    val distributedMgr = injector.instance[DistributedManager]

    message.replyToMessageId match {
      case Some(replyToMessageId) =>
        (message.kind, message.version) match {
          case (DistributedMessageKind.CreateLabelResponse, 1) =>
            try {
              // Deserialize message data
              val createLabelResponse = fromJson(message.data)

              // Create result
              val result = DistributedResult(DistributedMessageKind.CreateLabelResponse, createLabelResponse.toJson)

              // Put response on channel
              distributedMgr.putResponseOnChannel(replyToMessageId, result)
            } catch {
              case e: Exception =>
                logger.warn(e)
                distributedMgr.putErrorOnChannel(replyToMessageId, ErrorCode.unexpectedError)
            }

          case (kind, version) =>
            logger.warn(s"Unsupported message -- ${kind} v${version}")
            distributedMgr.putErrorOnChannel(replyToMessageId, ErrorCode.unsupportedResponseMessage)
        }

      case None =>
        logger.warn(s"ReplyToMessageId not included in response")
    }
  }
}

case class CreateLabelResponse(
  label: Label
) extends ToJsonCapable
