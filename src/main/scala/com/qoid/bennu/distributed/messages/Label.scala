package com.qoid.bennu.distributed.messages

import com.qoid.bennu.JsonAssist.JValue
import com.qoid.bennu.ToJsonCapable
import com.qoid.bennu.model.Label
import com.qoid.bennu.model.id.InternalId
import com.qoid.bennu.model.id.SemanticId

case class CreateLabelRequest(parentLabelIid: InternalId, name: String, semanticId: Option[SemanticId], data: JValue) extends ToJsonCapable
case class CreateLabelResponse(label: Label) extends ToJsonCapable

case class UpdateLabelRequest(labelIid: InternalId, name: Option[String], data: Option[JValue]) extends ToJsonCapable
case class UpdateLabelResponse(label: Label) extends ToJsonCapable

case class MoveLabelRequest(labelIid: InternalId, oldParentLabelIid: InternalId, newParentLabelIid: InternalId) extends ToJsonCapable
case class MoveLabelResponse(labelIid: InternalId) extends ToJsonCapable

case class CopyLabelRequest(labelIid: InternalId, newParentLabelIid: InternalId) extends ToJsonCapable
case class CopyLabelResponse(labelIid: InternalId) extends ToJsonCapable

case class RemoveLabelRequest(labelIid: InternalId, parentLabelIid: InternalId) extends ToJsonCapable
case class RemoveLabelResponse(labelIid: InternalId) extends ToJsonCapable

case class GrantLabelAccessRequest(labelIid: InternalId, connectionIid: InternalId, maxDoV: Int) extends ToJsonCapable
case class GrantLabelAccessResponse(labelIid: InternalId) extends ToJsonCapable

case class RevokeLabelAccessRequest(labelIid: InternalId, connectionIid: InternalId) extends ToJsonCapable
case class RevokeLabelAccessResponse(labelIid: InternalId) extends ToJsonCapable

case class UpdateLabelAccessRequest(labelIid: InternalId, connectionIid: InternalId, maxDoV: Int) extends ToJsonCapable
case class UpdateLabelAccessResponse(labelIid: InternalId) extends ToJsonCapable
