package com.qoid.bennu.webservices.v1

import com.google.inject.Inject
import com.qoid.bennu.BennuException
import com.qoid.bennu.ErrorCode
import com.qoid.bennu.JsonAssist._
import com.qoid.bennu.distributed.DistributedMessageKind
import com.qoid.bennu.distributed.messages.CreateLabelRequest
import com.qoid.bennu.model.id.InternalId
import com.qoid.bennu.model.id.SemanticId
import com.qoid.bennu.webservices.DistributedService
import m3.predef._
import m3.servlet.beans.Parm

case class CreateLabel @Inject()(
  injector: ScalaInjector,
  @Parm route: List[InternalId] = Nil,
  @Parm parentLabelIid: InternalId,
  @Parm name: String,
  @Parm semanticId: Option[SemanticId] = None,
  @Parm data: JValue = JNothing
) extends DistributedService {

  override protected val request = CreateLabelRequest(parentLabelIid, name, semanticId, data).toJson

  def doPost(): Unit = {
    run(injector, DistributedMessageKind.CreateLabelRequest, route)
  }

  override protected def validateParameters(): Unit = {
    if (name.isEmpty) throw new BennuException(ErrorCode.nameInvalid)
  }
}
