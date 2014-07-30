package com.qoid.bennu.webservices.v1

import com.google.inject.Inject
import com.qoid.bennu.BennuException
import com.qoid.bennu.ErrorCode
import com.qoid.bennu.JsonAssist._
import com.qoid.bennu.distributed.DistributedMessageKind
import com.qoid.bennu.distributed.messages.CreateContentRequest
import com.qoid.bennu.model.id.InternalId
import com.qoid.bennu.webservices.DistributedService
import m3.predef._
import m3.servlet.beans.Parm

case class CreateContent @Inject()(
  injector: ScalaInjector,
  @Parm route: List[InternalId],
  @Parm contentType: String,
  @Parm data: JValue,
  @Parm labelIids: List[InternalId]
) extends DistributedService {

  override protected val request = CreateContentRequest(contentType, data, labelIids).toJson

  def doPost(): Unit = {
    run(injector, DistributedMessageKind.CreateContentRequest, route)
  }

  override protected def validateParameters(): Unit = {
    if (contentType.isEmpty) throw new BennuException(ErrorCode.contentTypeInvalid)
    if (data == JNothing) throw new BennuException(ErrorCode.dataInvalid)
    if (labelIids.isEmpty) throw new BennuException(ErrorCode.labelIidsInvalid)
  }
}
