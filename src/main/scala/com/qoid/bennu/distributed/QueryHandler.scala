package com.qoid.bennu.distributed

import com.qoid.bennu.AgentView
import com.qoid.bennu.FromJsonCapable
import com.qoid.bennu.JdbcAssist._
import com.qoid.bennu.JsonAssist._
import com.qoid.bennu.JsonAssist.jsondsl._
import com.qoid.bennu.model.Connection
import com.qoid.bennu.model.InternalId
import java.sql.{ Connection => JdbcConn }
import m3.json.Json
import m3.predef._
import net.codingwell.scalaguice.InjectorExtensions.ScalaInjector
import com.qoid.bennu.ToJsonCapable
import com.qoid.bennu.webservices.DistributedQueryService.RequestData
import com.qoid.bennu.squery.StandingQueryManager
import com.qoid.bennu.squery.StandingQuery
import com.qoid.bennu.SecurityContext.AgentCapableSecurityContext
import com.qoid.bennu.squery.StandingQueryEvent
import com.qoid.bennu.squery.StandingQueryEvent
import com.qoid.bennu.model.HasInternalId
import m3.servlet.longpoll.ChannelId
import m3.servlet.longpoll.Channel
import m3.servlet.longpoll.ChannelManager
import com.qoid.bennu.model.AsyncResponse
import com.qoid.bennu.webservices.DistributedQueryService.ResponseData
import com.qoid.bennu.model.AsyncResponseType

object QueryHandler extends Logging {
  
  val channelManager = inject[ChannelManager]
  
  def process(aliasIid: Option[InternalId], connectionIid: Option[InternalId], request: RequestData, injector: ScalaInjector): JValue = {

    implicit val jdbcConn = injector.instance[JdbcConn]
    val agentView = injector.instance[AgentView]
    
    val mapper = findMapperByTypeName(request.tpe)

    if ( request.leaveStanding ) {
      val sQueryMgr = injector.instance[StandingQueryManager]
      sQueryMgr.add(
        StandingQuery(
          agentId = agentView.securityContext.agentId,
          handle = request.handle,
          context = request.context,
          securityContext = agentView.securityContext,
          typeQueries = List(StandingQuery.TypeQuery(request.tpe, Some(request.query))),
          listener = channelListener(aliasIid, connectionIid, channelManager.channel(request.channelId), request.handle, request.context)
        )
      )
    }

    if ( request.historical ) 
      JArray(agentView.select(request.query)(mapper).map(_.toJson).toList)
    else 
      JNothing
      
  }
  
  def channelListener(
      aliasIid: Option[InternalId], 
      connectionIid: Option[InternalId],
      channel: Channel, 
      handle: InternalId,
      context: JValue
  ): StandingQueryEvent => Unit = {
    event: StandingQueryEvent => 
      val data = ResponseData(aliasIid, connectionIid, event.tpe.toLowerCase(), Some(JArray(List(event.instance))))
//      val response = AsyncResponse(AsyncResponseType.SQuery, handle, true, data.toJson, context = context)
      val response = AsyncResponse(AsyncResponseType.Query, handle, true, data.toJson, context = context)
      channel.put(response.toJson)
  }
  
}

class QueryHandler extends DistributedRequestHandler {
  def handle(dr: DistributedRequest, connection: Connection)(implicit jdbcConn: JdbcConn): DistributedResponse = {
    val agentView = inject[AgentView]
    val request = RequestData.fromJson(dr.data)
    val results = QueryHandler.process(None, Some(connection.iid), request, inject[ScalaInjector])
    DistributedResponse(
      dr.iid,
      connection.localPeerId,
      connection.remotePeerId,
      results
    )
  }
}
