package com.qoid.bennu.distributed.handlers

import com.qoid.bennu.ErrorCode
import com.qoid.bennu.JsonAssist
import com.qoid.bennu.JsonAssist._
import com.qoid.bennu.distributed.DistributedMessage
import com.qoid.bennu.distributed.DistributedMessageKind
import com.qoid.bennu.distributed.DistributedRequestHandler
import com.qoid.bennu.distributed.DistributedResponseHandler
import com.qoid.bennu.distributed.messages
import com.qoid.bennu.mapper.MapperAssist
import com.qoid.bennu.model.service.QueryResult
import com.qoid.bennu.query.StandingQuery
import com.qoid.bennu.query.StandingQueryRepository
import com.qoid.bennu.query.ast
import com.qoid.bennu.security.SecurityContext
import m3.predef._
import m3.servlet.HttpResponseException
import m3.servlet.HttpStatusCodes

object QueryRequest extends DistributedRequestHandler[messages.QueryRequest] {
  override protected val requestKind = DistributedMessageKind.QueryRequest
  override protected val responseKind = DistributedMessageKind.QueryResponse
  override protected val allowedVersions = List(1)

  override protected def validateRequest(request: messages.QueryRequest): Unit = {
    if (!MapperAssist.allMappers.exists(_.typeName =:= request.tpe)) {
      throw new HttpResponseException(HttpStatusCodes.BAD_REQUEST, ErrorCode.typeInvalid)
    }

    try {
      ast.Query.parse(request.query)
    } catch {
      case _: Exception => throw new HttpResponseException(HttpStatusCodes.BAD_REQUEST, ErrorCode.queryInvalid)
    }

    if (!request.historical && !request.standing) {
      throw new HttpResponseException(HttpStatusCodes.BAD_REQUEST, ErrorCode.historicalStandingInvalid)
    }
  }

  override def process(message: DistributedMessage, request: messages.QueryRequest, injector: ScalaInjector): JValue = {
    if (request.standing) {
      val standingQueryRepo = injector.instance[StandingQueryRepository]

      val standingQuery = StandingQuery(
        injector.instance[SecurityContext].agentId,
        request.tpe,
        request.query,
        message.messageId,
        message.replyRoute
      )

      standingQueryRepo.save(standingQuery)
    }

    if (request.historical) {
      val mapper = MapperAssist.findMapperByTypeName(request.tpe)
      val results = mapper.select(request.query).map(JsonAssist.toJson).toList
      messages.QueryResponse(request.tpe, results).toJson
    } else {
      messages.QueryResponse(request.tpe, List()).toJson
    }
  }
}

object QueryResponse extends DistributedResponseHandler[messages.QueryResponse] {
  override protected val responseKind = DistributedMessageKind.QueryResponse
  override protected val allowedVersions = List(1)

  override protected def getServiceResult(response: messages.QueryResponse): JValue = {
    QueryResult(false, response.tpe, response.results, None).toJson
  }
}

object StandingQueryResponse extends DistributedResponseHandler[messages.StandingQueryResponse] {
  override protected val responseKind = DistributedMessageKind.StandingQueryResponse
  override protected val allowedVersions = List(1)

  override protected def getServiceResult(response: messages.StandingQueryResponse): JValue = {
    QueryResult(true, response.tpe, List(response.result), Some(response.action)).toJson
  }
}
