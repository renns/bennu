package com.qoid.bennu.webservices

import com.google.inject.Inject
import com.qoid.bennu.JdbcAssist
import com.qoid.bennu.SecurityContext.AgentCapableSecurityContext
import com.qoid.bennu.model._
import com.qoid.bennu.squery._
import java.sql.Connection
import m3.predef._
import m3.servlet.beans.JsonRequestBody
import m3.servlet.beans.Parm
import m3.servlet.longpoll.ChannelManager
import net.liftweb.json._

case class DeleteService @Inject() (
  implicit conn: Connection,
  requestBody: JsonRequestBody,
  securityContext: AgentCapableSecurityContext,
  channelMgr: ChannelManager,
  sQueryMgr: StandingQueryManager,
  @Parm("type") tpe: String,
  @Parm("primaryKey") iid: InternalId
) extends Logging {

  def service: JValue = {

    // Get the mapper for the passed in type
    val mapper = JdbcAssist.findMapperByTypeName(tpe).asInstanceOf[JdbcAssist.BennuMapperCompanion[HasInternalId.GenericInstance]]

    // Get the instance
    val i = mapper.fetch(iid)

    // Perform soft delete
    val di = mapper.softDelete(i)

    // Get any standing queries
    val sQueries = sQueryMgr.get(securityContext.agentId, tpe)

    // Send results to standing queries
    for (sQuery <- sQueries) {
      val action = StandingQueryAction.Delete
      val event = StandingQueryEvent(action, sQuery.handle, tpe, di.toJson)
      val channel = channelMgr.channel(sQuery.channelId)
      channel.put(event.toJson)
    }

    requestBody.body
  }
}
