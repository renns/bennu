package com.qoid.bennu.webservices.v1

import com.google.inject.Inject
import com.qoid.bennu.BennuException
import com.qoid.bennu.ErrorCode
import com.qoid.bennu.distributed.DistributedMessageKind
import com.qoid.bennu.distributed.messages.UpdateLabelAccessRequest
import com.qoid.bennu.model.id.InternalId
import com.qoid.bennu.webservices.DistributedService
import m3.predef._
import m3.servlet.beans.Parm

case class UpdateLabelAccess @Inject()(
  injector: ScalaInjector,
  @Parm route: List[InternalId],
  @Parm labelIid: InternalId,
  @Parm connectionIid: InternalId,
  @Parm maxDoV: Int
) extends DistributedService {

  override protected val request = UpdateLabelAccessRequest(labelIid, connectionIid, maxDoV).toJson

  def doPost(): Unit = {
    run(injector, DistributedMessageKind.UpdateLabelAccessRequest, route)
  }

  override protected def validateParameters(): Unit = {
    if (maxDoV < 1) throw new BennuException(ErrorCode.maxDoVInvalid)
  }
}
