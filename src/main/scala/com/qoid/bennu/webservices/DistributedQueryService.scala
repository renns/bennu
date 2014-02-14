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
import com.qoid.bennu.squery.ast.Query
import com.qoid.bennu.squery.ast.Transformer
import com.qoid.bennu.squery.ast.ContentQuery
import com.qoid.bennu.distributed.DistributedManager
import com.qoid.bennu.distributed.DistributedRequestKind
import scala.concurrent.ExecutionContext.Implicits.global
import m3.servlet.longpoll.ChannelManager
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
        case Failure(t) => logger.warn(t)
      }
    }

    "handle" -> handle.value  
    
  }

}

