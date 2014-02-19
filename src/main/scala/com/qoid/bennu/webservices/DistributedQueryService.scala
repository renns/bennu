package com.qoid.bennu.webservices

import com.google.inject.Inject
import com.qoid.bennu.JsonAssist._
import com.qoid.bennu.SecurityContext.AgentCapableSecurityContext
import com.qoid.bennu.distributed.DistributedManager
import com.qoid.bennu.distributed.DistributedRequestKind
import com.qoid.bennu.model._
import java.sql.Connection
import jsondsl._
import m3.predef._
import m3.servlet.beans.Parm
import m3.servlet.longpoll.ChannelId
import m3.servlet.longpoll.ChannelManager
import scala.concurrent.ExecutionContext.Implicits.global
import scala.language.existentials
import scala.util.Failure
import scala.util.Success

case class DistributedQueryService @Inject()(
  implicit conn: Connection,
  distributedMgr: DistributedManager,
  channelId: ChannelId,
  channelMgr: ChannelManager,
  securityContext: AgentCapableSecurityContext,
  @Parm("type") _type: String,
  @Parm("q") queryStr: String,
  @Parm connectionIids: List[InternalId]
) extends Logging {

  def service = {
    
    val handle = InternalId.random

    for (connectionIid <- connectionIids) {
      distributedMgr.sendRequest(connectionIid, DistributedRequestKind.Query, ("type"->_type) ~ ("q" -> queryStr)).onComplete {
        case Success(data) =>
          val responseData = ("connectionIid" -> connectionIid) ~ ("results" -> data)
          val response = AsyncResponse(AsyncResponseType.Query, handle, responseData)
          val channel = channelMgr.channel(channelId)
          channel.put(response.toJson)
        case Failure(t) => logger.warn(s"distributed query: FAIL -- $t")
      }
    }

    "handle" -> handle.value  
  }
}
