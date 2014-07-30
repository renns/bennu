package com.qoid.bennu.webservices.v1

import com.google.inject.Inject
import com.qoid.bennu.BennuException
import com.qoid.bennu.ErrorCode
import com.qoid.bennu.distributed.DistributedMessageKind
import com.qoid.bennu.distributed.messages.UpdateAliasProfileRequest
import com.qoid.bennu.model.id.InternalId
import com.qoid.bennu.webservices.DistributedService
import m3.predef._
import m3.servlet.beans.Parm

case class UpdateAliasProfile @Inject()(
  injector: ScalaInjector,
  @Parm route: List[InternalId],
  @Parm aliasIid: InternalId,
  @Parm profileName: Option[String],
  @Parm profileImage: Option[String]
) extends DistributedService {

  override protected val request = UpdateAliasProfileRequest(aliasIid, profileName, profileImage).toJson

  def doPost(): Unit = {
    run(injector, DistributedMessageKind.UpdateAliasProfileRequest, route)
  }

  override protected def validateParameters(): Unit = {
    if (profileName.isEmpty && profileImage.isEmpty) throw new BennuException(ErrorCode.profileNameProfileImageInvalid)
  }
}
