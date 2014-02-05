package com.qoid.bennu.webservices

import com.google.inject.Inject
import com.qoid.bennu.{ErrorCode, ServiceException, JdbcAssist}
import com.qoid.bennu.SecurityContext.AgentCapableSecurityContext
import com.qoid.bennu.model.HasInternalId
import com.qoid.bennu.squery._
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

    val agentView = securityContext.createView

    val result = mapper.fetchOpt(instance.iid) match {
      case None => doUpsert(true, agentView.validateInsert, mapper.insert, instance)
      case _ => doUpsert(false, agentView.validateUpdate, mapper.update, instance)
    }

    result.toJson
  }

  private def doUpsert(
    isInsert: Boolean,
    validateFn: HasInternalId => Box[HasInternalId],
    upsertFn: HasInternalId => HasInternalId,
    instance: HasInternalId
  ): HasInternalId = {

    validateFn(instance) match {
      case Full(i) =>
        val result = upsertFn(i)
        notifyStandingQueries(isInsert, result)
        result
      case _ => throw ServiceException("Security validation failed", ErrorCode.SecurityValidationFailed)
    }
  }

  private def notifyStandingQueries(isInsert: Boolean, instance: HasInternalId): Unit = {
    val sQueries = sQueryMgr.get(securityContext.agentId, tpe)

    for (sQuery <- sQueries) {
      val action = if (isInsert) StandingQueryAction.Insert else StandingQueryAction.Update
      val event = StandingQueryEvent(action, sQuery.handle, tpe, instance.toJson)
      val channel = channelMgr.channel(sQuery.channelId)
      channel.put(event.toJson)
    }
  }
}
