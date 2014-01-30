package com.qoid.bennu.webservices

import com.google.inject.Inject
import com.qoid.bennu.model._
import com.qoid.bennu.squery._
import m3.predef._
import m3.servlet.beans.Parm
import m3.servlet.longpoll.ChannelId
import scala.language.existentials

case class RegisterStandingQueryService @Inject()(
  sQueryMgr: StandingQueryManager,
  @Parm types: List[String],
  @Parm handle: InternalId,
  @Parm agentId: AgentId,
  @Parm channelId: ChannelId
) extends Logging {

  def service = {
    types.foreach( tpe =>
      sQueryMgr.add(
        StandingQuery(agentId, channelId, handle, tpe)
      )
    )
    //TODO: Return something
  }
}
