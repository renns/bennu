package com.qoid.bennu.webservices.v1

import com.google.inject.Inject
import com.qoid.bennu.JsonAssist._
import com.qoid.bennu.distributed.DistributedMessageKind
import com.qoid.bennu.distributed.messages.UpdateAliasRequest
import com.qoid.bennu.model.id.InternalId
import com.qoid.bennu.webservices.DistributedService
import m3.predef._
import m3.servlet.beans.Parm

case class UpdateAlias @Inject()(
  injector: ScalaInjector,
  @Parm route: List[InternalId] = Nil,
  @Parm aliasIid: InternalId,
  @Parm data: JValue
) extends DistributedService {

  override protected val request = UpdateAliasRequest(aliasIid, data).toJson

  def doPost(): Unit = {
    run(injector, DistributedMessageKind.UpdateAliasRequest, route)
  }
}
