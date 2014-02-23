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

object QueryHandler extends Logging {
  
  def process(request: RequestData, injector: ScalaInjector): JValue = {

    implicit val jdbcConn = injector.instance[JdbcConn]
    val agentView = injector.instance[AgentView]
    
    val mapper = findMapperByTypeName(request.tpe)

    if ( request.leaveStanding ) {
      val sQueryMgr = injector.instance[StandingQueryManager]
      sQueryMgr.add(
        StandingQuery(
          agentId = agentView.securityContext.agentId,
          channelId = request.channelId,
          handle = request.handle,
          context = request.context,
          securityContext = agentView.securityContext,
          typeQueries = List(StandingQuery.TypeQuery(request.tpe, Some(request.query)))
        )
      )
    }

    if ( request.historical ) 
      JArray(agentView.select(request.query)(mapper).map(_.toJson).toList)
    else 
      JNothing
      
  }

  
}

class QueryHandler extends DistributedRequestHandler {
  def handle(dr: DistributedRequest, connection: Connection)(implicit jdbcConn: JdbcConn): DistributedResponse = {
    val agentView = inject[AgentView]
    val request = RequestData.fromJson(dr.data)
    val results = QueryHandler.process(request, inject[ScalaInjector])
    DistributedResponse(
      dr.iid,
      connection.localPeerId,
      connection.remotePeerId,
      results
    )
  }
}
