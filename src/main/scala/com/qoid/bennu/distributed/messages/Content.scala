package com.qoid.bennu.distributed.messages

import com.qoid.bennu.JsonAssist._
import com.qoid.bennu.ToJsonCapable
import com.qoid.bennu.model.Content
import com.qoid.bennu.model.id.InternalId
import com.qoid.bennu.model.id.SemanticId

case class CreateContentRequest(contentType: String, semanticId: Option[SemanticId], data: JValue, labelIids: List[InternalId]) extends ToJsonCapable
case class CreateContentResponse(content: Content) extends ToJsonCapable

case class UpdateContentRequest(contentIid: InternalId, data: JValue) extends ToJsonCapable
case class UpdateContentResponse(content: Content) extends ToJsonCapable

case class AddContentLabelRequest(contentIid: InternalId, labelIid: InternalId) extends ToJsonCapable
case class AddContentLabelResponse(contentIid: InternalId, labelIid: InternalId) extends ToJsonCapable

case class RemoveContentLabelRequest(contentIid: InternalId, labelIid: InternalId) extends ToJsonCapable
case class RemoveContentLabelResponse(contentIid: InternalId, labelIid: InternalId) extends ToJsonCapable
