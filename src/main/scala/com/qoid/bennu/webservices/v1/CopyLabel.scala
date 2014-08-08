package com.qoid.bennu.webservices.v1

import com.google.inject.Inject
import com.qoid.bennu.distributed.DistributedMessageKind
import com.qoid.bennu.distributed.messages.CopyLabelRequest
import com.qoid.bennu.model.id.InternalId
import com.qoid.bennu.webservices.DistributedService
import m3.predef._
import m3.servlet.beans.Parm

case class CopyLabel @Inject()(
  injector: ScalaInjector,
  @Parm route: List[InternalId],
  @Parm labelIid: InternalId,
  @Parm newParentLabelIid: InternalId
) extends DistributedService {

  override protected val request = CopyLabelRequest(labelIid, newParentLabelIid).toJson

  def doPost(): Unit = {
    run(injector, DistributedMessageKind.CopyLabelRequest, route)
  }
}
