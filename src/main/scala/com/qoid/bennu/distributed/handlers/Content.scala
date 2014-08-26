package com.qoid.bennu.distributed.handlers

import com.qoid.bennu.BennuException
import com.qoid.bennu.ErrorCode
import com.qoid.bennu.JsonAssist._
import com.qoid.bennu.JsonAssist.jsondsl._
import com.qoid.bennu.distributed.DistributedMessage
import com.qoid.bennu.distributed.DistributedMessageKind
import com.qoid.bennu.distributed.DistributedRequestHandler
import com.qoid.bennu.distributed.DistributedResponseHandler
import com.qoid.bennu.distributed.messages
import com.qoid.bennu.model.Content
import com.qoid.bennu.model.LabeledContent
import m3.jdbc._
import m3.predef._

object CreateContentRequest extends DistributedRequestHandler[messages.CreateContentRequest] {
  override protected val requestKind = DistributedMessageKind.CreateContentRequest
  override protected val responseKind = DistributedMessageKind.CreateContentResponse
  override protected val allowedVersions = List(1)

  override def process(message: DistributedMessage, request: messages.CreateContentRequest, injector: ScalaInjector): JValue = {
    if (request.contentType.isEmpty) throw new BennuException(ErrorCode.contentTypeInvalid)
    if (request.data == JNothing) throw new BennuException(ErrorCode.dataInvalid)
    if (request.labelIids.isEmpty) throw new BennuException(ErrorCode.labelIidsInvalid)

    val content = Content.insert(Content(request.contentType, data = request.data))

    request.labelIids.foreach { labelIid =>
      LabeledContent.insert(LabeledContent(content.iid, labelIid))
    }

    messages.CreateContentResponse(content).toJson
  }
}

object CreateContentResponse extends DistributedResponseHandler[messages.CreateContentResponse] {
  override protected val responseKind = DistributedMessageKind.CreateContentResponse
  override protected val allowedVersions = List(1)

  override protected def getServiceResult(response: messages.CreateContentResponse, message: DistributedMessage): JValue = {
    response.content.toJson
  }
}

object UpdateContentRequest extends DistributedRequestHandler[messages.UpdateContentRequest] {
  override protected val requestKind = DistributedMessageKind.UpdateContentRequest
  override protected val responseKind = DistributedMessageKind.UpdateContentResponse
  override protected val allowedVersions = List(1)

  override def process(message: DistributedMessage, request: messages.UpdateContentRequest, injector: ScalaInjector): JValue = {
    if (request.data == JNothing) throw new BennuException(ErrorCode.dataInvalid)

    val content = Content.fetch(request.contentIid)
    val content2 = Content.update(content.copy(data = request.data))
    messages.UpdateContentResponse(content2).toJson
  }
}

object UpdateContentResponse extends DistributedResponseHandler[messages.UpdateContentResponse] {
  override protected val responseKind = DistributedMessageKind.UpdateContentResponse
  override protected val allowedVersions = List(1)

  override protected def getServiceResult(response: messages.UpdateContentResponse, message: DistributedMessage): JValue = {
    response.content.toJson
  }
}

object AddContentLabelRequest extends DistributedRequestHandler[messages.AddContentLabelRequest] {
  override protected val requestKind = DistributedMessageKind.AddContentLabelRequest
  override protected val responseKind = DistributedMessageKind.AddContentLabelResponse
  override protected val allowedVersions = List(1)

  override def process(message: DistributedMessage, request: messages.AddContentLabelRequest, injector: ScalaInjector): JValue = {
    if (LabeledContent.selectOpt(sql"contentIid = ${request.contentIid} and labelIid = ${request.labelIid}").nonEmpty) {
      throw new BennuException(ErrorCode.contentAlreadyHasLabel)
    }

    LabeledContent.insert(LabeledContent(request.contentIid, request.labelIid))
    messages.AddContentLabelResponse(request.contentIid, request.labelIid).toJson
  }
}

object AddContentLabelResponse extends DistributedResponseHandler[messages.AddContentLabelResponse] {
  override protected val responseKind = DistributedMessageKind.AddContentLabelResponse
  override protected val allowedVersions = List(1)

  override protected def getServiceResult(response: messages.AddContentLabelResponse, message: DistributedMessage): JValue = {
    ("contentIid" -> response.contentIid) ~ ("labelIid" -> response.labelIid)
  }
}

object RemoveContentLabelRequest extends DistributedRequestHandler[messages.RemoveContentLabelRequest] {
  override protected val requestKind = DistributedMessageKind.RemoveContentLabelRequest
  override protected val responseKind = DistributedMessageKind.RemoveContentLabelResponse
  override protected val allowedVersions = List(1)

  override def process(message: DistributedMessage, request: messages.RemoveContentLabelRequest, injector: ScalaInjector): JValue = {
    if (LabeledContent.selectOpt(sql"contentIid = ${request.contentIid} and labelIid = ${request.labelIid}").isEmpty) {
      throw new BennuException(ErrorCode.contentDoesNotHaveLabel)
    }

    val labeledContent = LabeledContent.selectOne(sql"contentIid = ${request.contentIid} and labelIid = ${request.labelIid}")
    LabeledContent.delete(labeledContent)
    messages.RemoveContentLabelResponse(request.contentIid, request.labelIid).toJson
  }
}

object RemoveContentLabelResponse extends DistributedResponseHandler[messages.RemoveContentLabelResponse] {
  override protected val responseKind = DistributedMessageKind.RemoveContentLabelResponse
  override protected val allowedVersions = List(1)

  override protected def getServiceResult(response: messages.RemoveContentLabelResponse, message: DistributedMessage): JValue = {
    ("contentIid" -> response.contentIid) ~ ("labelIid" -> response.labelIid)
  }
}
