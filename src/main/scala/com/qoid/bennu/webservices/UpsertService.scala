package com.qoid.bennu.webservices

import com.google.inject.Inject
import com.qoid.bennu.JdbcAssist
import com.qoid.bennu.SecurityContext.AgentCapableSecurityContext
import com.qoid.bennu.model.HasInternalId
import com.qoid.bennu.squery._
import java.sql.Connection
import m3.json.JsonSerializer
import m3.predef._
import m3.servlet.beans.JsonRequestBody
import m3.servlet.beans.Parm
import m3.servlet.longpoll._
import net.liftweb.json.JValue
import scala.language.existentials

case class UpsertService @Inject()(
  implicit conn: Connection,
  serializer: JsonSerializer,
  requestBody: JsonRequestBody,
  securityContext: AgentCapableSecurityContext,
  channelMgr: ChannelManager,
  sQueryMgr: StandingQueryManager,
  @Parm("type") tpe: String,
  @Parm instance: JValue
) extends Logging {

  def service: JValue = {

    // Get the mapper for the passed in type
    val mapper = JdbcAssist.findMapperByTypeName(tpe).asInstanceOf[JdbcAssist.BennuMapperCompanion[HasInternalId.GenericInstance]]

    // Deserialize the passed in instance
    val i = mapper.fromJson(instance)

    // Check whether this is an insert or update
    val insert = mapper.fetchOpt(i.iid) match {
      case None => true
      case _ => false
    }

    // Perform the insert / update
    if (insert) {
      mapper.insert(i)
    } else {
      mapper.update(i)
    }

    // Get any standing queries
    val sQueries = sQueryMgr.get(securityContext.agentId, tpe)

    // Send results to standing queries
    for (sQuery <- sQueries) {
      val action = if (insert) StandingQueryAction.Insert else StandingQueryAction.Update
      val event = StandingQueryEvent(action, sQuery.handle, tpe, i.toJson)
      val channel = channelMgr.channel(sQuery.channelId)
      channel.put(event.toJson)
    }

    requestBody.body
  }
}
