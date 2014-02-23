package com.qoid.bennu.webservices

import com.google.inject.Inject
import com.qoid.bennu.ErrorCode
import com.qoid.bennu.JdbcAssist
import com.qoid.bennu.SecurityContext.AgentCapableSecurityContext
import com.qoid.bennu.ServiceException
import com.qoid.bennu.model.HasInternalId
import com.qoid.bennu.squery._
import java.sql.Connection
import m3.predef._
import m3.predef.box._
import m3.servlet.beans.Parm
import net.liftweb.json._
import scala.language.existentials

case class UpsertService @Inject()(
  implicit conn: Connection,
  securityContext: AgentCapableSecurityContext,
  @Parm("type") tpe: String,
  @Parm("instance") instanceJson: JValue
) extends Logging {

  def service: JValue = {
    val mapper = JdbcAssist.findMapperByTypeName(tpe).asInstanceOf[JdbcAssist.BennuMapperCompanion[HasInternalId]]
    val instance = mapper.fromJson(instanceJson).copy2(agentId=securityContext.agentId)
    val agentView = securityContext.createView

    val result = mapper.fetchOpt(instance.iid) match {
      case None =>
        // This is an insert
        agentView.validateInsert(instance) match {
          case Full(i) => mapper.insert(i).notifyStandingQueries(StandingQueryAction.Insert)
          case _ => throw ServiceException("Security validation failed", ErrorCode.SecurityValidationFailed)
        }
      case _ =>
        // This is an update
        agentView.validateUpdate(instance) match {
          case Full(i) => mapper.update(i).notifyStandingQueries(StandingQueryAction.Update)
          case _ => throw ServiceException("Security validation failed", ErrorCode.SecurityValidationFailed)
        }
    }

    result.toJson
  }
}
