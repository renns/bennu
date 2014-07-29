package com.qoid.bennu.webservices.v1

import com.google.inject.Inject
import com.qoid.bennu.BennuException
import com.qoid.bennu.ErrorCode
import com.qoid.bennu.distributed.DistributedManager
import com.qoid.bennu.distributed.DistributedMessage
import com.qoid.bennu.distributed.DistributedMessageKind
import com.qoid.bennu.distributed.RequestData
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

/**
* Performs a query against the database.
*
* Parameters:
* - route: Array of Strings
* - type: String
* - query: String
* - historical: Boolean
* - standing: Boolean
*
* Response Values: None
*
* Error Codes:
* - routeInvalid
* - typeInvalid
* - queryInvalid
* - historicalStandingInvalid
*/
case class Query @Inject()(
  session: Session,
  queryMgr: QueryManager,
  distributedMgr: DistributedManager,
  methodInvocation: MethodInvocation,
  @Parm route: List[InternalId],
  @Parm("type") tpe: String,
  @Parm query: String,
  @Parm historical: Boolean,
  @Parm standing: Boolean
) extends Logging {

  def doPost(): Unit = {
    try {
      validateParameters()

      val queryRequest = QueryRequest(tpe, query, historical, standing)
      val message = DistributedMessage(DistributedMessageKind.QueryRequest, 1, route, queryRequest.toJson)
      val requestData = RequestData(session.channel.id, methodInvocation.context, !standing)

      if (standing) {
        session.addStandingQuery(methodInvocation.context, message.messageId)
      }

      distributedMgr.sendRequest(message, requestData)
    } catch {
      case e: BennuException =>
        throw new HttpResponseException(HttpStatusCodes.BAD_REQUEST, e.getErrorCode()).initCause(e)
    }
  }

  private def validateParameters(): Unit = {
    if (route.isEmpty) throw new HttpResponseException(HttpStatusCodes.BAD_REQUEST, ErrorCode.routeInvalid)
    if (!MapperAssist.allMappers.exists(_.typeName =:= tpe)) throw new HttpResponseException(HttpStatusCodes.BAD_REQUEST, ErrorCode.typeInvalid)

    try {
      ast.Query.parse(query)
    } catch {
      case _: Exception => throw new HttpResponseException(HttpStatusCodes.BAD_REQUEST, ErrorCode.queryInvalid)
    }

    if (!historical && !standing) throw new HttpResponseException(HttpStatusCodes.BAD_REQUEST, ErrorCode.historicalStandingInvalid)
  }
}
