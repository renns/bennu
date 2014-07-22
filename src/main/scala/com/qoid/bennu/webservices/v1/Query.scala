package com.qoid.bennu.webservices.v1

import com.google.inject.Inject
import com.qoid.bennu.ErrorCode
import com.qoid.bennu.distributed.DistributedManager
import com.qoid.bennu.distributed.messages.DistributedMessage
import com.qoid.bennu.distributed.messages.DistributedMessageKind
import com.qoid.bennu.distributed.messages.QueryRequest
import com.qoid.bennu.mapper.MapperAssist
import com.qoid.bennu.model.id.InternalId
import com.qoid.bennu.session.Session
import com.qoid.bennu.query.QueryManager
import com.qoid.bennu.query.ast
import m3.predef._
import m3.servlet.HttpResponseException
import m3.servlet.HttpStatusCodes
import m3.servlet.beans.MultiRequestHandler.MethodInvocation
import m3.servlet.beans.Parm

case class Query @Inject()(
  session: Session,
  queryMgr: QueryManager,
  distributedMgr: DistributedManager,
  methodInvocation: MethodInvocation,
  @Parm("type") tpe: String,
  @Parm query: String,
  @Parm routes: List[List[InternalId]],
  @Parm historical: Boolean,
  @Parm standing: Boolean
) extends Logging {

  def doPost(): Unit = {
    validateParameters()

    routes.foreach { route =>
      val queryRequest = QueryRequest(tpe, query, historical, standing)
      val message = DistributedMessage(DistributedMessageKind.QueryRequest, 1, route, queryRequest.toJson)

      if (standing) {
        session.addStandingQuery(methodInvocation.context, message.messageId, route)
      }

      queryMgr.addQuery(message.messageId, route, session.channel.id, methodInvocation.context, standing)

      distributedMgr.send(message)
    }
  }

  private def validateParameters(): Unit = {
    if (!MapperAssist.allMappers.exists(_.typeName =:= tpe)) {
      throw new HttpResponseException(HttpStatusCodes.BAD_REQUEST, ErrorCode.typeInvalid)
    }

    try {
      ast.Query.parse(query)
    } catch {
      case _: Exception => throw new HttpResponseException(HttpStatusCodes.BAD_REQUEST, ErrorCode.queryInvalid)
    }

    if (routes.isEmpty || routes.exists(_.isEmpty)) {
      throw new HttpResponseException(HttpStatusCodes.BAD_REQUEST, ErrorCode.routesInvalid)
    }

    if (!historical && !standing) {
      throw new HttpResponseException(HttpStatusCodes.BAD_REQUEST, ErrorCode.historicalStandingInvalid)
    }
  }
}
