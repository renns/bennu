package com.qoid.bennu.webservices.v1

import com.google.inject.Inject
import com.qoid.bennu.BennuException
import com.qoid.bennu.ErrorCode
import com.qoid.bennu.JsonAssist._
import com.qoid.bennu.distributed.DistributedMessageKind
import com.qoid.bennu.distributed.messages.CreateNotificationRequest
import com.qoid.bennu.model.id.InternalId
import com.qoid.bennu.webservices.DistributedService
import m3.predef._
import m3.servlet.beans.Parm

case class CreateNotification @Inject()(
  injector: ScalaInjector,
  @Parm route: List[InternalId],
  @Parm kind: String,
  @Parm data: JValue = JNothing
) extends DistributedService {

  override protected val request = CreateNotificationRequest(kind, data).toJson

  def doPost(): Unit = {
    run(injector, DistributedMessageKind.CreateNotificationRequest, route)
  }

  override protected def validateParameters(): Unit = {
    if (kind.isEmpty) throw new BennuException(ErrorCode.kindInvalid)
  }
}
