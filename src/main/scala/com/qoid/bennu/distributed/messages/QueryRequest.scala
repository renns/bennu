package com.qoid.bennu.distributed.messages

import com.qoid.bennu.FromJsonCapable
import com.qoid.bennu.JsonAssist
import com.qoid.bennu.JsonAssist.jsondsl._
import com.qoid.bennu.ToJsonCapable
import com.qoid.bennu.distributed.DistributedManager
import com.qoid.bennu.mapper.MapperAssist
import com.qoid.bennu.query.StandingQuery
import com.qoid.bennu.query.StandingQueryRepository
import com.qoid.bennu.security.SecurityContext
import m3.json.Json
import m3.predef._

object QueryRequest extends FromJsonCapable[QueryRequest] with Logging {
  def handle(message: DistributedMessage, injector: ScalaInjector): Unit = {
    (message.kind, message.version) match {
      case (DistributedMessageKind.QueryRequest, 1) =>
        val queryRequest = fromJson(message.data)

        //TODO: validate
        if (queryRequest.historical) {
          val distributedMgr = injector.instance[DistributedManager]
          val mapper = MapperAssist.findMapperByTypeName(queryRequest.tpe)
          val results = mapper.select(queryRequest.query).map(JsonAssist.toJson).toList

          val responseData = QueryResponse(queryRequest.tpe, results)

          val responseMessage = DistributedMessage(
            DistributedMessageKind.QueryResponse,
            1,
            message.replyRoute,
            responseData.toJson,
            Some(message.messageId)
          )

          distributedMgr.send(responseMessage)
        }

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

      case (kind, version) => logger.warn(s"unsupported distributed message -- ${kind} v${version}")
    }
  }
}

case class QueryRequest(
  @Json("type") tpe: String,
  query: String,
  historical: Boolean,
  standing: Boolean
) extends ToJsonCapable
