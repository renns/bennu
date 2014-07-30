package com.qoid.bennu.webservices.v1

import com.google.inject.Inject
import com.qoid.bennu.BennuException
import com.qoid.bennu.ErrorCode
import com.qoid.bennu.JsonAssist._
import com.qoid.bennu.distributed.DistributedMessageKind
import com.qoid.bennu.distributed.messages.CreateAliasRequest
import com.qoid.bennu.model.id.InternalId
import com.qoid.bennu.webservices.DistributedService
import m3.predef._
import m3.servlet.beans.Parm

case class CreateAlias @Inject()(
  injector: ScalaInjector,
  @Parm route: List[InternalId],
  @Parm name: String,
  @Parm profileName: String,
  @Parm profileImage: String = "",
  @Parm data: JValue = JNothing
) extends DistributedService {

  override protected val request = CreateAliasRequest(name, profileName, profileImage, data).toJson

  def doPost(): Unit = {
    run(injector, DistributedMessageKind.CreateAliasRequest, route)
  }

  override protected def validateParameters(): Unit = {
    if (name.isEmpty) throw new BennuException(ErrorCode.nameInvalid)
    if (profileName.isEmpty) throw new BennuException(ErrorCode.profileNameInvalid)
  }
}
