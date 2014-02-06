package com.qoid.bennu.webservices

import com.google.inject.Inject
import com.qoid.bennu.ErrorCode
import com.qoid.bennu.JdbcAssist
import com.qoid.bennu.SecurityContext.AgentCapableSecurityContext
import com.qoid.bennu.ServiceException
import com.qoid.bennu.model.HasInternalId
import com.qoid.bennu.security.ChannelMap
import com.qoid.bennu.squery._
import com.qoid.bennu.squery.ast.Evaluator
import com.qoid.bennu.squery.ast.Query
import java.sql.Connection
import m3.json.JsonSerializer
import m3.predef._
import m3.predef.box._
import m3.servlet.beans.Parm
import m3.servlet.longpoll._
import net.liftweb.json._
import scala.language.existentials

case class UpsertService @Inject()(
  implicit conn: Connection,
  serializer: JsonSerializer,
  securityContext: AgentCapableSecurityContext,
  channelMgr: ChannelManager,
  sQueryMgr: StandingQueryManager,
  @Parm("type") tpe: String,
  @Parm("instance") instanceJson: JValue
) extends Logging {

  def service: JValue = {
    val mapper = JdbcAssist.findMapperByTypeName(tpe).asInstanceOf[JdbcAssist.BennuMapperCompanion[HasInternalId]]
    val instance = mapper.fromJson(instanceJson)

    val result = mapper.fetchOpt(instance.iid) match {
      case None => doUpsert(true, mapper, instance)
      case _ => doUpsert(false, mapper, instance)
    }

    result.toJson
  }

  private def doUpsert(
    isInsert: Boolean,
    mapper: JdbcAssist.BennuMapperCompanion[HasInternalId],
    instance: HasInternalId
  ): HasInternalId = {

    val agentView = securityContext.createView

    val validateResult = if (isInsert) agentView.validateInsert(instance) else agentView.validateUpdate(instance)

    validateResult match {
      case Full(i) =>
        val result = if (isInsert) mapper.insert(i) else mapper.update(i)
        notifyStandingQueries(isInsert, mapper, result)
        result
      case _ => throw ServiceException("Security validation failed", ErrorCode.SecurityValidationFailed)
    }
  }

  private def notifyStandingQueries(
    isInsert: Boolean,
    mapper: JdbcAssist.BennuMapperCompanion[HasInternalId],
    instance: HasInternalId
  ): Unit = {

    val sQueries = sQueryMgr.get(securityContext.agentId, tpe)

    for {
      sQuery <- sQueries
      sc <- ChannelMap.channelToSecurityContextMap.get(sQuery.channelId)
      if Evaluator.evaluateQuery(sc.createView.constrict(mapper, Query.nil), instance) == Evaluator.VTrue
    } {
      val action = if (isInsert) StandingQueryAction.Insert else StandingQueryAction.Update
      val event = StandingQueryEvent(action, sQuery.handle, tpe, instance.toJson)
      val channel = channelMgr.channel(sQuery.channelId)
      channel.put(event.toJson)
    }
  }
}
