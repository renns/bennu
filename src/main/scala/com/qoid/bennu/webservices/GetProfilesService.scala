package com.qoid.bennu.webservices

import com.google.inject.Inject
import com.qoid.bennu.distributed.DistributedManager
import com.qoid.bennu.model._
import java.sql.{ Connection => JdbcConn }
import m3.servlet.beans.Parm
import com.qoid.bennu.JsonAssist._
import com.qoid.bennu.JsonAssist.jsondsl._

case class GetProfilesService @Inject() (
  implicit
  jdbcConn: JdbcConn,
  distributedMgr: DistributedManager,
  @Parm connectionIids: List[InternalId]
) {
  def service: JValue = {
    for (
      connectionIid <- connectionIids
    ) yield connectionIid.value -> distributedMgr.getProfile(connectionIid)
  }
}
