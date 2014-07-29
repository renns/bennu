package com.qoid.bennu.distributed.messages

import com.qoid.bennu.BennuException
import com.qoid.bennu.ErrorCode
import com.qoid.bennu.FromJsonCapable
import com.qoid.bennu.JsonAssist._
import com.qoid.bennu.ToJsonCapable
import com.qoid.bennu.distributed.DistributedManager
import com.qoid.bennu.model.Label
import com.qoid.bennu.model.LabelChild
import com.qoid.bennu.model.id.InternalId
import m3.predef._

object CreateLabelRequest extends FromJsonCapable[CreateLabelRequest] with Logging {
  def handle(message: DistributedMessage, injector: ScalaInjector): Unit = {
    val distributedMgr = injector.instance[DistributedManager]

    (message.kind, message.version) match {
      case (DistributedMessageKind.CreateLabelRequest, 1) =>
        try {
          // Deserialize message data
          val createLabelRequest = fromJson(message.data)

          // Validate
          if (createLabelRequest.name.isEmpty) {
            throw new BennuException(ErrorCode.nameInvalid)
          }

          // Create label
          val label = Label.insert(Label(createLabelRequest.name, data = createLabelRequest.data))
          LabelChild.insert(LabelChild(createLabelRequest.parentLabelIid, label.iid))

          // Create response
          val response = CreateLabelResponse(label)
          val responseMessage = DistributedMessage(
            DistributedMessageKind.CreateLabelResponse,
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

case class CreateLabelRequest(
  parentLabelIid: InternalId,
  name: String,
  data: JValue
) extends ToJsonCapable
