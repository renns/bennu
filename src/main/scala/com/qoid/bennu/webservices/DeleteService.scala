package com.qoid.bennu.webservices

import com.google.inject.Inject
import com.qoid.bennu.SecurityContext.AgentCapableSecurityContext
import com.qoid.bennu._
import com.qoid.bennu.model._
import com.qoid.bennu.security.ChannelMap
import com.qoid.bennu.squery._
import com.qoid.bennu.squery.ast.Evaluator
import com.qoid.bennu.squery.ast.Query
import java.sql.Connection
import m3.predef._
import m3.servlet.beans.Parm
import m3.servlet.longpoll.ChannelManager
import net.liftweb.common.Full
import net.liftweb.json._

case class DeleteService @Inject() (
  implicit conn: Connection,
  securityContext: AgentCapableSecurityContext,
  channelMgr: ChannelManager,
  sQueryMgr: StandingQueryManager,
  @Parm("type") tpe: String,
  @Parm("primaryKey") iid: InternalId
) extends Logging {

  def service: JValue = {
    val mapper = JdbcAssist.findMapperByTypeName(tpe).asInstanceOf[JdbcAssist.BennuMapperCompanion[HasInternalId]]
    val instance = mapper.fetch(iid)
    val agentView = securityContext.createView

    agentView.validateDelete(instance) match {
      case Full(i) =>
        val result = mapper.softDelete(i)
        notifyStandingQueries(mapper, result)
        result.toJson
      case _ => throw ServiceException("Security validation failed", ErrorCode.SecurityValidationFailed)
    }
  }

  private def notifyStandingQueries(
    mapper: JdbcAssist.BennuMapperCompanion[HasInternalId],
    instance: HasInternalId
  ): Unit = {

    val sQueries = sQueryMgr.get(securityContext.agentId, tpe)

    for {
      sQuery <- sQueries
      sc <- ChannelMap.channelToSecurityContextMap.get(sQuery.channelId)
      if Evaluator.evaluateQuery(sc.createView.constrict(mapper, Query.nil), instance) == Evaluator.VTrue
    } {
      val action = StandingQueryAction.Delete
      val event = StandingQueryEvent(action, sQuery.handle, tpe, instance.toJson)
      val channel = channelMgr.channel(sQuery.channelId)
      channel.put(event.toJson)
    }
  }
}
