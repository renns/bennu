
package com.qoid.bennu.webservices

import com.google.inject.Inject
import com.qoid.bennu.model.AgentId
import m3.servlet.beans.Parm
import m3.servlet.longpoll.ChannelManager
import com.qoid.bennu.model.Agent
import java.sql.Connection
import com.qoid.bennu.JsonAssist._
import com.qoid.bennu.SecurityContext
import com.qoid.bennu.security.ChannelMap
import com.qoid.bennu.model.InternalId
import m3.servlet.ForbiddenException
import m3.predef._
import box._

case class CreateChannel @Inject() (
  implicit 
  conn: Connection,
  manager: ChannelManager,
  @Parm authenticationId: InternalId
) {
  
  def service = {
    ChannelMap.authenticate(authenticationId) match {
      case Full(channelId) => jobj("id", JString(channelId.value))
      case _ => throw new ForbiddenException("authentication failed")
    }
  }
  
}
