package com.qoid.bennu.distributed.messages

import com.qoid.bennu.ErrorCode
import com.qoid.bennu.FromJsonCapable
import com.qoid.bennu.JsonAssist._
import com.qoid.bennu.ToJsonCapable
import com.qoid.bennu.distributed.DistributedHandler
import com.qoid.bennu.distributed.DistributedManager
import com.qoid.bennu.distributed.DistributedMessage
import com.qoid.bennu.distributed.DistributedMessageKind
import com.qoid.bennu.distributed.DistributedResult
import m3.json.Json
import m3.predef._

object QueryResponse extends DistributedHandler with FromJsonCapable[QueryResponse] with Logging {
  def handle(message: DistributedMessage, injector: ScalaInjector): Unit = {
    val distributedMgr = injector.instance[DistributedManager]

    message.replyToMessageId match {
      case Some(replyToMessageId) =>
        (message.kind, message.version) match {
          case (DistributedMessageKind.QueryResponse, 1) =>
            try {
              // Deserialize message data
              val response = fromJson(message.data)

              // Create result
              val result = DistributedResult(DistributedMessageKind.QueryResponse, response.toJson)

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

case class QueryResponse(
  @Json("type") tpe: String,
  results: List[JValue]
) extends ToJsonCapable
