package com.qoid.bennu.webservices.v1

import com.google.inject.Inject
import com.qoid.bennu.distributed.DistributedMessageKind
import com.qoid.bennu.distributed.messages.AddContentLabelRequest
import com.qoid.bennu.model.id.InternalId
import com.qoid.bennu.webservices.DistributedService
import m3.predef._
import m3.servlet.beans.Parm

case class AddContentLabel @Inject()(
  injector: ScalaInjector,
  @Parm route: List[InternalId],
  @Parm contentIid: InternalId,
  @Parm labelIid: InternalId
) extends DistributedService {

  override protected val request = AddContentLabelRequest(contentIid, labelIid).toJson

  def doPost(): Unit = {
    run(injector, DistributedMessageKind.AddContentLabelRequest, route)
  }
}
