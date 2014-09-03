package com.qoid.bennu.webservices.v1

import com.google.inject.Inject
import com.qoid.bennu.BennuException
import com.qoid.bennu.ErrorCode
import com.qoid.bennu.distributed.DistributedMessageKind
import com.qoid.bennu.distributed.messages.InitiateIntroductionRequest
import com.qoid.bennu.model.id.InternalId
import com.qoid.bennu.webservices.DistributedService
import m3.predef._
import m3.servlet.beans.Parm

case class InitiateIntroduction @Inject()(
  injector: ScalaInjector,
  @Parm route: List[InternalId] = Nil,
  @Parm aConnectionIid: InternalId,
  @Parm aMessage: String,
  @Parm bConnectionIid: InternalId,
  @Parm bMessage: String
) extends DistributedService {

  override protected val request = InitiateIntroductionRequest(aConnectionIid, aMessage, bConnectionIid, bMessage).toJson

  def doPost(): Unit = {
    run(injector, DistributedMessageKind.InitiateIntroductionRequest, route)
  }

  override protected def validateParameters(): Unit = {
    if (aMessage.isEmpty) throw new BennuException(ErrorCode.aMessageInvalid)
    if (bMessage.isEmpty) throw new BennuException(ErrorCode.bMessageInvalid)
  }
}
