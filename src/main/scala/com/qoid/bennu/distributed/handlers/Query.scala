package com.qoid.bennu.distributed.handlers

import com.qoid.bennu.BennuException
import com.qoid.bennu.ErrorCode
import com.qoid.bennu.JsonAssist
import com.qoid.bennu.JsonAssist._
import com.qoid.bennu.distributed.DistributedHandler
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

  override def process(message: DistributedMessage, request: messages.QueryRequest, injector: ScalaInjector): JValue = {
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

  override protected def getServiceResult(response: messages.QueryResponse, message: DistributedMessage): JValue = {
    QueryResult(message.replyRoute, false, response.tpe, response.results, None).toJson
  }
}

object StandingQueryResponse extends DistributedResponseHandler[messages.StandingQueryResponse] {
  override protected val responseKind = DistributedMessageKind.StandingQueryResponse
  override protected val allowedVersions = List(1)

  override protected def getServiceResult(response: messages.StandingQueryResponse, message: DistributedMessage): JValue = {
    QueryResult(message.replyRoute, true, response.tpe, List(response.result), Some(response.action)).toJson
  }
}

object CancelQueryRequest extends DistributedHandler with Logging {
  private val requestKind = DistributedMessageKind.CancelQueryRequest
  private val allowedVersions = List(1)

  def handle(message: DistributedMessage, injector: ScalaInjector): Unit = {
    message.replyToMessageId match {
      case Some(replyToMessageId) =>
        try {
          if (message.kind != requestKind || !allowedVersions.contains(message.version)) {
            throw new BennuException(ErrorCode.unsupportedMessage, s"${message.kind} v${message.version}")
          }

          val standingQueryRepo = injector.instance[StandingQueryRepository]
          standingQueryRepo.delete(injector.instance[SecurityContext].agentId, replyToMessageId, message.replyRoute)
        } catch {
          case e: BennuException =>
            logger.debug(s"BennuException: ${e.getErrorCode()} -- ${e.getMessage}")
          case e: Exception =>
            logger.warn(e)
        }

      case None => logger.warn(s"ReplyToMessageId not included in cancel query request")
    }
  }
}
