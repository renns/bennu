package com.qoid.bennu.webservices

import com.google.inject.Inject
import com.qoid.bennu.JsonAssist._
import com.qoid.bennu.JsonAssist.jsondsl._
import com.qoid.bennu.security.ChannelMap
import java.sql.Connection
import m3.predef.box._
import m3.servlet.ForbiddenException
import m3.servlet.beans.Parm

case class CreateChannel @Inject() (
  implicit 
  conn: Connection,
  @Parm authenticationId: String
) {

  def service: JValue = {
    ChannelMap.authenticate(authenticationId) match {
      case Full((channelId, aliasIid)) => ("channelId" -> channelId.value) ~ ("aliasIid" -> aliasIid)
      case _ => throw new ForbiddenException("authentication failed")
    }
  }
}
