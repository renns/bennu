package com.qoid.bennu.distributed

import com.qoid.bennu.AgentView
import com.qoid.bennu.FromJsonCapable
import com.qoid.bennu.JdbcAssist._
import com.qoid.bennu.JsonAssist.jsondsl._
import com.qoid.bennu.model.Connection
import com.qoid.bennu.model.InternalId
import java.sql.{ Connection => JdbcConn }
import m3.json.Json
import m3.predef._

object QueryHandler {
  object Request extends FromJsonCapable[Request]

  case class Request(
    @Json("type") tpe: String,
    query: String,
    leaveStanding: Boolean,
    handle: InternalId
  )
}

class QueryHandler extends DistributedRequestHandler {
  def handle(dr: DistributedRequest, connection: Connection)(implicit jdbcConn: JdbcConn): DistributedResponse = {
    val agentView = inject[AgentView]

    val request = QueryHandler.Request.fromJson(dr.data)
    val mapper = findMapperByTypeName(request.tpe)
    val results = agentView.select(request.query)(mapper).toList

    //TODO: kick off standing query if leaveStanding is true

    DistributedResponse(
      dr.iid,
      connection.localPeerId,
      connection.remotePeerId,
      results.map(_.toJson)
    )
  }
}
