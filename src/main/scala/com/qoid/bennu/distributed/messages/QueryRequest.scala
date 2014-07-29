package com.qoid.bennu.distributed.messages

import com.qoid.bennu.BennuException
import com.qoid.bennu.ErrorCode
import com.qoid.bennu.FromJsonCapable
import com.qoid.bennu.JsonAssist
import com.qoid.bennu.ToJsonCapable
import com.qoid.bennu.distributed.DistributedManager
import com.qoid.bennu.mapper.MapperAssist
import com.qoid.bennu.query.StandingQuery
import com.qoid.bennu.query.StandingQueryRepository
import com.qoid.bennu.query.ast
import com.qoid.bennu.security.SecurityContext
import m3.json.Json
import m3.predef._
import m3.servlet.HttpResponseException
import m3.servlet.HttpStatusCodes

object QueryRequest extends FromJsonCapable[QueryRequest] with Logging {
  def handle(message: DistributedMessage, injector: ScalaInjector): Unit = {
    val distributedMgr = injector.instance[DistributedManager]

    (message.kind, message.version) match {
      case (DistributedMessageKind.QueryRequest, 1) =>
        try {
          // Deserialize message data
          val queryRequest = fromJson(message.data)

          // Validate
          if (!MapperAssist.allMappers.exists(_.typeName =:= queryRequest.tpe)) {
            throw new HttpResponseException(HttpStatusCodes.BAD_REQUEST, ErrorCode.typeInvalid)
          }

          try {
            ast.Query.parse(queryRequest.query)
          } catch {
            case _: Exception => throw new HttpResponseException(HttpStatusCodes.BAD_REQUEST, ErrorCode.queryInvalid)
          }

          if (!queryRequest.historical && !queryRequest.standing) {
            throw new HttpResponseException(HttpStatusCodes.BAD_REQUEST, ErrorCode.historicalStandingInvalid)
          }

          if (queryRequest.historical) {
            // Query historical
            val mapper = MapperAssist.findMapperByTypeName(queryRequest.tpe)
            val results = mapper.select(queryRequest.query).map(JsonAssist.toJson).toList

            // Create response
            val response = QueryResponse(queryRequest.tpe, results)
            val responseMessage = DistributedMessage(
              DistributedMessageKind.QueryResponse,
              1,
              message.replyRoute,
              response.toJson,
              Some(message.messageId)
            )

            // Send response
            distributedMgr.sendResponse(responseMessage)
          }

          // Setup standing query
          if (queryRequest.standing) {
            val standingQueryRepo = injector.instance[StandingQueryRepository]

            val standingQuery = StandingQuery(
              injector.instance[SecurityContext].agentId,
              queryRequest.tpe,
              queryRequest.query,
              message.messageId,
              message.replyRoute
            )

            standingQueryRepo.save(standingQuery)
          }
        } catch {
          case e: BennuException =>
            logger.debug(s"BennuException: ${e.getErrorCode()} -- ${e.getMessage}")
            distributedMgr.sendError(e, message)
          case e: Exception =>
            logger.warn(e)
            distributedMgr.sendError(ErrorCode.unexpectedError, e.getMessage, message)
        }

      case (kind, version) =>
        logger.warn(s"Unsupported message -- ${kind} v${version}")
        distributedMgr.sendError(ErrorCode.unsupportedMessage, s"${kind} ${version}", message)
    }
  }
}

case class QueryRequest(
  @Json("type") tpe: String,
  query: String,
  historical: Boolean,
  standing: Boolean
) extends ToJsonCapable
