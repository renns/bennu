package com.qoid.bennu.webservices.v1

import com.google.inject.Inject
import com.qoid.bennu.BennuException
import com.qoid.bennu.ErrorCode
import com.qoid.bennu.JsonAssist._
import com.qoid.bennu.distributed.DistributedMessageKind
import com.qoid.bennu.distributed.messages.UpdateLabelRequest
import com.qoid.bennu.model.id.InternalId
import com.qoid.bennu.webservices.DistributedService
import m3.predef._
import m3.servlet.beans.Parm

case class UpdateLabel @Inject()(
  injector: ScalaInjector,
  @Parm route: List[InternalId],
  @Parm labelIid: InternalId,
  @Parm name: Option[String] = None,
  @Parm data: Option[JValue] = None
) extends DistributedService {

  override protected val request = UpdateLabelRequest(labelIid, name, data).toJson

  def doPost(): Unit = {
    run(injector, DistributedMessageKind.UpdateLabelRequest, route)
  }

  override protected def validateParameters(): Unit = {
    name.foreach(n => if (n.isEmpty) throw new BennuException(ErrorCode.nameInvalid))
    if (name.isEmpty && data.isEmpty) throw new BennuException(ErrorCode.nameDataInvalid)
  }
}
