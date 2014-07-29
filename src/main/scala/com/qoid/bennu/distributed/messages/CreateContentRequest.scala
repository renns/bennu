package com.qoid.bennu.distributed.messages

import com.qoid.bennu.BennuException
import com.qoid.bennu.ErrorCode
import com.qoid.bennu.FromJsonCapable
import com.qoid.bennu.JsonAssist._
import com.qoid.bennu.ToJsonCapable
import com.qoid.bennu.distributed.DistributedManager
import com.qoid.bennu.distributed.DistributedMessage
import com.qoid.bennu.distributed.DistributedMessageKind
import com.qoid.bennu.model.Content
import com.qoid.bennu.model.LabeledContent
import com.qoid.bennu.model.id.InternalId
import m3.predef._

object CreateContentRequest extends FromJsonCapable[CreateContentRequest] with Logging {
  def handle(message: DistributedMessage, injector: ScalaInjector): Unit = {
    val distributedMgr = injector.instance[DistributedManager]

    (message.kind, message.version) match {
      case (DistributedMessageKind.CreateContentRequest, 1) =>
        try {
          // Deserialize message data
          val request = fromJson(message.data)

          // Validate
          if (request.contentType.isEmpty) throw new BennuException(ErrorCode.contentTypeInvalid)
          if (request.data == JNothing) throw new BennuException(ErrorCode.dataInvalid)
          if (request.labelIids.isEmpty) throw new BennuException(ErrorCode.labelIidsInvalid)

          // Create content
          val content = Content.insert(Content(request.contentType, data = request.data))

          request.labelIids.foreach { labelIid =>
            LabeledContent.insert(LabeledContent(content.iid, labelIid))
          }

          // Create response
          val response = CreateContentResponse(content)
          val responseMessage = DistributedMessage(
            DistributedMessageKind.CreateContentResponse,
            1,
            message.replyRoute,
            response.toJson,
            Some(message.messageId)
          )

          // Send response
          distributedMgr.sendResponse(responseMessage)
        } catch {
          case e: BennuException =>
            logger.debug(s"BennuException: ${e.getErrorCode()} -- ${e.getMessage}")
            distributedMgr.sendError(e, message)
          case e: Exception =>
            logger.warn(e)
            distributedMgr.sendError(ErrorCode.unexpectedError, e.getMessage, message)
        }

      case (kind, version) =>
        logger.warn(s"Unsupported message -- ${kind} v${version}")
        distributedMgr.sendError(ErrorCode.unsupportedMessage, s"${kind} ${version}", message)
    }
  }
}

case class CreateContentRequest(
  contentType: String,
  data: JValue,
  labelIids: List[InternalId]
) extends ToJsonCapable
