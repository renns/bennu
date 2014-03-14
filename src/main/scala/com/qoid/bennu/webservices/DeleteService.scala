package com.qoid.bennu.webservices

import com.google.inject.Inject
import com.qoid.bennu._
import com.qoid.bennu.model._
import com.qoid.bennu.security.SecurityContext
import com.qoid.bennu.squery._
import java.sql.Connection
import m3.predef._
import m3.servlet.beans.Parm
import net.liftweb.common.Full
import net.liftweb.json._

case class DeleteService @Inject() (
  implicit conn: Connection,
  securityContext: SecurityContext,
  @Parm("type") tpe: String,
  @Parm("primaryKey") iid: InternalId
) extends Logging {

  def service: JValue = {
    val mapper = JdbcAssist.findMapperByTypeName(tpe).asInstanceOf[JdbcAssist.BennuMapperCompanion[HasInternalId]]
    val instance = mapper.fetch(iid)
    val agentView = securityContext.createView

    agentView.validateDelete(instance) match {
      case Full(i) => mapper.softDelete(i).notifyStandingQueries(StandingQueryAction.Delete).toJson
      case _ => throw ServiceException("Security validation failed", ErrorCode.SecurityValidationFailed)
    }
  }
}
