package com.qoid.bennu.webservices.v1

import com.google.inject.Inject
import com.qoid.bennu.BennuException
import com.qoid.bennu.ErrorCode
import com.qoid.bennu.JsonAssist._
import com.qoid.bennu.distributed.DistributedMessageKind
import com.qoid.bennu.distributed.messages.UpdateContentRequest
import com.qoid.bennu.model.id.InternalId
import com.qoid.bennu.webservices.DistributedService
import m3.predef._
import m3.servlet.beans.Parm

case class UpdateContent @Inject()(
  injector: ScalaInjector,
  @Parm route: List[InternalId],
  @Parm contentIid: InternalId,
  @Parm data: JValue
) extends DistributedService {

  override protected val request = UpdateContentRequest(contentIid, data).toJson

  def doPost(): Unit = {
    run(injector, DistributedMessageKind.UpdateContentRequest, route)
  }

  override protected def validateParameters(): Unit = {
    if (data == JNothing) throw new BennuException(ErrorCode.dataInvalid)
  }
}
