package com.qoid.bennu.webservices

import com.google.inject.Inject
import com.qoid.bennu.model
import com.qoid.bennu.model._
import java.sql.Connection
import m3.predef._
import m3.servlet.beans.JsonRequestBody
import m3.servlet.beans.Parm
import com.qoid.bennu.JdbcAssist
import m3.servlet.longpoll.ChannelId
import com.qoid.bennu.SecurityContext
import com.qoid.bennu.SecurityContext.AgentCapableSecurityContext

case class DeleteService @Inject() (
  conn: Connection,
  requestBody: JsonRequestBody,
  securityContext: AgentCapableSecurityContext,
  @Parm("type") _type: String,
  @Parm("primaryKey") iid: InternalId
) extends Logging {

  implicit def _conn = conn

  def service = {

    val mapper = JdbcAssist.findMapperByTypeName(_type)

    mapper.softDeleteViaKey(iid)

    requestBody.body
  }
}
