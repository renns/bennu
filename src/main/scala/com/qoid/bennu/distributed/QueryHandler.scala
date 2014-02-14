package com.qoid.bennu.distributed

import com.qoid.bennu.JsonAssist._
import com.qoid.bennu.AgentView
import com.qoid.bennu.model.Connection
import com.qoid.bennu.model.Notification
import com.qoid.bennu.model.NotificationListener
import com.qoid.bennu.squery.StandingQueryAction
import java.sql.{ Connection => JdbcConn }
import m3.predef._
import com.qoid.bennu.JdbcAssist._
import com.qoid.bennu.SecurityContext

object QueryHandler {
  case class Request(_type: String, query: String)
}


class QueryHandler extends DistributedRequestHandler {
  
  
  def handle(dr: DistributedRequest, connection: Connection)(implicit jdbcConn: JdbcConn): DistributedResponse = {
    
    val sc = inject[SecurityContext]
    val av = inject[AgentView]
    
    val request = dr.data.deserialize[QueryHandler.Request]
    val mapper = findMapperByTypeName(request._type)
      
    av.select(request.query)(mapper)

    DistributedResponse(
      dr.iid,
      connection.localPeerId,
      connection.remotePeerId,
      JNothing
    )
  }
}
