package com.qoid.bennu.webservices

import com.google.inject.Inject
import com.qoid.bennu.model
import com.qoid.bennu.model._
import java.sql.Connection
import m3.jdbc._
import m3.json.JsonSerializer
import m3.predef._
import m3.servlet.beans.Parm
import net.liftweb.json.JValue
import com.qoid.bennu.model.InternalId
import com.qoid.bennu.model.HasInternalId
import m3.servlet.beans.JsonRequestBody
import scala.language.existentials
import com.qoid.bennu.JdbcAssist._
import com.qoid.bennu.JsonAssist._
import jsondsl._
import m3.servlet.longpoll.ChannelId
import com.qoid.bennu.SecurityContext
import com.qoid.bennu.SecurityContext.AgentCapableSecurityContext

case class QueryService @Inject()(
  implicit conn: Connection,
  channelId: ChannelId,
  securityContext: AgentCapableSecurityContext,
  @Parm("type") _type: String,
  @Parm("q") userWhere: String
) extends Logging {

  def service = {
    val mapper = findMapperByTypeName(_type)
    val partialWhere = 
      if ( userWhere.isBlank ) "1 = 1"
      else userWhere
    val fullWhere = partialWhere + " and deleted = false"
    JArray(
      mapper.
        select(fullWhere).
        map(_.toJson).
        toList
    )
  }
  
}

