package com.qoid.bennu.webservices.v1

import com.google.inject.Inject
import com.qoid.bennu.distributed.DistributedMessageKind
import com.qoid.bennu.distributed.messages.UpdateAliasLoginRequest
import com.qoid.bennu.model.id.InternalId
import com.qoid.bennu.security.AuthenticationManager
import com.qoid.bennu.webservices.DistributedService
import m3.predef._
import m3.servlet.beans.Parm

case class UpdateAliasLogin @Inject()(
  injector: ScalaInjector,
  authenticationMgr: AuthenticationManager,
  @Parm route: List[InternalId] = Nil,
  @Parm aliasIid: InternalId,
  @Parm password: String
) extends DistributedService {

  override protected val request = UpdateAliasLoginRequest(aliasIid, password).toJson

  def doPost(): Unit = {
    run(injector, DistributedMessageKind.UpdateAliasLoginRequest, route)
  }

  override protected def validateParameters(): Unit = {
    authenticationMgr.validatePassword(password)
  }
}
