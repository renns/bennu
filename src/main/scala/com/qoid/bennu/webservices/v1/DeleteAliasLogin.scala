package com.qoid.bennu.webservices.v1

import com.google.inject.Inject
import com.qoid.bennu.distributed.DistributedMessageKind
import com.qoid.bennu.distributed.messages.DeleteAliasLoginRequest
import com.qoid.bennu.model.id.InternalId
import com.qoid.bennu.webservices.DistributedService
import m3.predef._
import m3.servlet.beans.Parm

case class DeleteAliasLogin @Inject()(
  injector: ScalaInjector,
  @Parm route: List[InternalId] = Nil,
  @Parm aliasIid: InternalId
) extends DistributedService {

  override protected val request = DeleteAliasLoginRequest(aliasIid).toJson

  def doPost(): Unit = {
    run(injector, DistributedMessageKind.DeleteAliasLoginRequest, route)
  }
}
