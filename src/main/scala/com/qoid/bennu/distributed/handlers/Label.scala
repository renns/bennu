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
import com.qoid.bennu.model.Label
import com.qoid.bennu.model.LabelAcl
import com.qoid.bennu.model.LabelChild
import com.qoid.bennu.security.Role
import m3.jdbc._
import m3.predef._

object CreateLabelRequest extends DistributedRequestHandler[messages.CreateLabelRequest] {
  override protected val requestKind = DistributedMessageKind.CreateLabelRequest
  override protected val responseKind = DistributedMessageKind.CreateLabelResponse
  override protected val allowedVersions = List(1)

  override def process(message: DistributedMessage, request: messages.CreateLabelRequest, injector: ScalaInjector): JValue = {
    if (request.name.isEmpty) throw new BennuException(ErrorCode.nameInvalid)

    val label = Label.insert(Label(request.name, data = request.data))
    LabelChild.insert(LabelChild(request.parentLabelIid, label.iid))
    messages.CreateLabelResponse(label).toJson
  }
}

object CreateLabelResponse extends DistributedResponseHandler[messages.CreateLabelResponse] {
  override protected val responseKind = DistributedMessageKind.CreateLabelResponse
  override protected val allowedVersions = List(1)

  override protected def getServiceResult(response: messages.CreateLabelResponse, message: DistributedMessage): JValue = {
    response.label.toJson
  }
}

object UpdateLabelRequest extends DistributedRequestHandler[messages.UpdateLabelRequest] {
  override protected val requestKind = DistributedMessageKind.UpdateLabelRequest
  override protected val responseKind = DistributedMessageKind.UpdateLabelResponse
  override protected val allowedVersions = List(1)

  override def process(message: DistributedMessage, request: messages.UpdateLabelRequest, injector: ScalaInjector): JValue = {
    request.name.foreach(n => if (n.isEmpty) throw new BennuException(ErrorCode.nameInvalid))

    val label = Label.fetch(request.labelIid)

    val label2 = (request.name, request.data) match {
      case (Some(name), Some(data)) => label.copy(name = name, data = data)
      case (Some(name), None) => label.copy(name = name)
      case (None, Some(data)) => label.copy(data = data)
      case _ => throw new BennuException(ErrorCode.nameDataInvalid)
    }

    val label3 = Label.update(label2)

    messages.UpdateLabelResponse(label3).toJson
  }
}

object UpdateLabelResponse extends DistributedResponseHandler[messages.UpdateLabelResponse] {
  override protected val responseKind = DistributedMessageKind.UpdateLabelResponse
  override protected val allowedVersions = List(1)

  override protected def getServiceResult(response: messages.UpdateLabelResponse, message: DistributedMessage): JValue = {
    response.label.toJson
  }
}

object MoveLabelRequest extends DistributedRequestHandler[messages.MoveLabelRequest] {
  override protected val requestKind = DistributedMessageKind.MoveLabelRequest
  override protected val responseKind = DistributedMessageKind.MoveLabelResponse
  override protected val allowedVersions = List(1)

  override def process(message: DistributedMessage, request: messages.MoveLabelRequest, injector: ScalaInjector): JValue = {
    val oldLabelChild = LabelChild.selectOne(sql"parentIid = ${request.oldParentLabelIid} and childIid = ${request.labelIid}")
    LabelChild.insert(LabelChild(request.newParentLabelIid, request.labelIid))
    LabelChild.delete(oldLabelChild)
    messages.MoveLabelResponse(request.labelIid).toJson
  }
}

object MoveLabelResponse extends DistributedResponseHandler[messages.MoveLabelResponse] {
  override protected val responseKind = DistributedMessageKind.MoveLabelResponse
  override protected val allowedVersions = List(1)

  override protected def getServiceResult(response: messages.MoveLabelResponse, message: DistributedMessage): JValue = {
    "labelIid" -> response.labelIid
  }
}

object CopyLabelRequest extends DistributedRequestHandler[messages.CopyLabelRequest] {
  override protected val requestKind = DistributedMessageKind.CopyLabelRequest
  override protected val responseKind = DistributedMessageKind.CopyLabelResponse
  override protected val allowedVersions = List(1)

  override def process(message: DistributedMessage, request: messages.CopyLabelRequest, injector: ScalaInjector): JValue = {
    LabelChild.insert(LabelChild(request.newParentLabelIid, request.labelIid))
    messages.CopyLabelResponse(request.labelIid).toJson
  }
}

object CopyLabelResponse extends DistributedResponseHandler[messages.CopyLabelResponse] {
  override protected val responseKind = DistributedMessageKind.CopyLabelResponse
  override protected val allowedVersions = List(1)

  override protected def getServiceResult(response: messages.CopyLabelResponse, message: DistributedMessage): JValue = {
    "labelIid" -> response.labelIid
  }
}

object RemoveLabelRequest extends DistributedRequestHandler[messages.RemoveLabelRequest] {
  override protected val requestKind = DistributedMessageKind.RemoveLabelRequest
  override protected val responseKind = DistributedMessageKind.RemoveLabelResponse
  override protected val allowedVersions = List(1)

  override def process(message: DistributedMessage, request: messages.RemoveLabelRequest, injector: ScalaInjector): JValue = {
    val labelChild = LabelChild.selectOne(sql"parentIid = ${request.parentLabelIid} and childIid = ${request.labelIid}")
    LabelChild.delete(labelChild)
    messages.RemoveLabelResponse(request.labelIid).toJson
  }
}

object RemoveLabelResponse extends DistributedResponseHandler[messages.RemoveLabelResponse] {
  override protected val responseKind = DistributedMessageKind.RemoveLabelResponse
  override protected val allowedVersions = List(1)

  override protected def getServiceResult(response: messages.RemoveLabelResponse, message: DistributedMessage): JValue = {
    "labelIid" -> response.labelIid
  }
}

object GrantLabelAccessRequest extends DistributedRequestHandler[messages.GrantLabelAccessRequest] {
  override protected val requestKind = DistributedMessageKind.GrantLabelAccessRequest
  override protected val responseKind = DistributedMessageKind.GrantLabelAccessResponse
  override protected val allowedVersions = List(1)

  override def process(message: DistributedMessage, request: messages.GrantLabelAccessRequest, injector: ScalaInjector): JValue = {
    if (request.maxDoV < 1) throw new BennuException(ErrorCode.maxDoVInvalid)

    if (LabelAcl.selectOpt(sql"labelIid = ${request.labelIid} and connectionIid = ${request.connectionIid} and role = ${Role.ContentViewer.toString}").nonEmpty) {
      throw new BennuException(ErrorCode.connectionAlreadyHasAccess)
    }

    LabelAcl.insert(LabelAcl(request.connectionIid, request.labelIid, Role.ContentViewer, request.maxDoV))

    messages.GrantLabelAccessResponse(request.labelIid).toJson
  }
}

object GrantLabelAccessResponse extends DistributedResponseHandler[messages.GrantLabelAccessResponse] {
  override protected val responseKind = DistributedMessageKind.GrantLabelAccessResponse
  override protected val allowedVersions = List(1)

  override protected def getServiceResult(response: messages.GrantLabelAccessResponse, message: DistributedMessage): JValue = {
    "labelIid" -> response.labelIid
  }
}

object RevokeLabelAccessRequest extends DistributedRequestHandler[messages.RevokeLabelAccessRequest] {
  override protected val requestKind = DistributedMessageKind.RevokeLabelAccessRequest
  override protected val responseKind = DistributedMessageKind.RevokeLabelAccessResponse
  override protected val allowedVersions = List(1)

  override def process(message: DistributedMessage, request: messages.RevokeLabelAccessRequest, injector: ScalaInjector): JValue = {
    LabelAcl.selectOpt(sql"labelIid = ${request.labelIid} and connectionIid = ${request.connectionIid} and role = ${Role.ContentViewer.toString}") match {
      case Some(labelAcl) => LabelAcl.delete(labelAcl)
      case None => throw new BennuException(ErrorCode.connectionDoesNotHaveAccess)
    }

    messages.RevokeLabelAccessResponse(request.labelIid).toJson
  }
}

object RevokeLabelAccessResponse extends DistributedResponseHandler[messages.RevokeLabelAccessResponse] {
  override protected val responseKind = DistributedMessageKind.RevokeLabelAccessResponse
  override protected val allowedVersions = List(1)

  override protected def getServiceResult(response: messages.RevokeLabelAccessResponse, message: DistributedMessage): JValue = {
    "labelIid" -> response.labelIid
  }
}

object UpdateLabelAccessRequest extends DistributedRequestHandler[messages.UpdateLabelAccessRequest] {
  override protected val requestKind = DistributedMessageKind.UpdateLabelAccessRequest
  override protected val responseKind = DistributedMessageKind.UpdateLabelAccessResponse
  override protected val allowedVersions = List(1)

  override def process(message: DistributedMessage, request: messages.UpdateLabelAccessRequest, injector: ScalaInjector): JValue = {
    if (request.maxDoV < 1) throw new BennuException(ErrorCode.maxDoVInvalid)

    LabelAcl.selectOpt(sql"labelIid = ${request.labelIid} and connectionIid = ${request.connectionIid} and role = ${Role.ContentViewer.toString}") match {
      case Some(labelAcl) => LabelAcl.update(labelAcl.copy(maxDegreesOfVisibility = request.maxDoV))
      case None => throw new BennuException(ErrorCode.connectionDoesNotHaveAccess)
    }

    messages.UpdateLabelAccessResponse(request.labelIid).toJson
  }
}

object UpdateLabelAccessResponse extends DistributedResponseHandler[messages.UpdateLabelAccessResponse] {
  override protected val responseKind = DistributedMessageKind.UpdateLabelAccessResponse
  override protected val allowedVersions = List(1)

  override protected def getServiceResult(response: messages.UpdateLabelAccessResponse, message: DistributedMessage): JValue = {
    "labelIid" -> response.labelIid
  }
}
