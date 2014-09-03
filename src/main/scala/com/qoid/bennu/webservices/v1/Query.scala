package com.qoid.bennu.webservices.v1

import com.google.inject.Inject
import com.qoid.bennu.ErrorCode
import com.qoid.bennu.distributed.DistributedMessage
import com.qoid.bennu.distributed.DistributedMessageKind
import com.qoid.bennu.distributed.messages.QueryRequest
import com.qoid.bennu.mapper.MapperAssist
import com.qoid.bennu.model.id.InternalId
import com.qoid.bennu.query.ast
import com.qoid.bennu.session.Session
import com.qoid.bennu.webservices.DistributedService
import m3.predef._
import m3.servlet.HttpResponseException
import m3.servlet.HttpStatusCodes
import m3.servlet.beans.MultiRequestHandler.MethodInvocation
import m3.servlet.beans.Parm

case class Query @Inject()(
  injector: ScalaInjector,
  session: Session,
  methodInvocation: MethodInvocation,
  @Parm route: List[InternalId] = Nil,
  @Parm("type") tpe: String,
  @Parm query: String,
  @Parm historical: Boolean,
  @Parm standing: Boolean
) extends DistributedService {

  override protected val request = QueryRequest(tpe, query, historical, standing).toJson
  override protected val singleResponse = !standing

  def doPost(): Unit = {
    run(injector, DistributedMessageKind.QueryRequest, route)
  }

  override protected def validateParameters(): Unit = {
    if (!MapperAssist.allMappers.exists(_.typeName =:= tpe)) {
      throw new HttpResponseException(HttpStatusCodes.BAD_REQUEST, ErrorCode.typeInvalid)
    }

    try {
      ast.Query.parse(query)
    } catch {
      case _: Exception => throw new HttpResponseException(HttpStatusCodes.BAD_REQUEST, ErrorCode.queryInvalid)
    }

    if (!historical && !standing) {
      throw new HttpResponseException(HttpStatusCodes.BAD_REQUEST, ErrorCode.historicalStandingInvalid)
    }
  }

  override protected def beforeSend(message: DistributedMessage): Unit = {
    if (standing) {
      session.addStandingQuery(methodInvocation.context, message.messageId, route)
    }
  }
}
