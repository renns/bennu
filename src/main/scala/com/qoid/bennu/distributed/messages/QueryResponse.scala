package com.qoid.bennu.distributed.messages

import com.qoid.bennu.FromJsonCapable
import com.qoid.bennu.JsonAssist._
import com.qoid.bennu.ToJsonCapable
import com.qoid.bennu.query.QueryManager
import m3.json.Json
import m3.predef._

object QueryResponse extends FromJsonCapable[QueryResponse] with Logging {
  def handle(message: DistributedMessage, injector: ScalaInjector): Unit = {
    (message.kind, message.version) match {
      case (DistributedMessageKind.QueryResponse, 1) =>
        val queryMgr = injector.instance[QueryManager]
        val queryResponse = fromJson(message.data)

        //TODO: validate (replyToMessageId must be set)

        queryMgr.handleQueryResponse(message.replyToMessageId.get, message.replyRoute, queryResponse)

      case (kind, version) => logger.warn(s"unsupported distributed message -- ${kind} v${version}")
    }
  }
}

case class QueryResponse(
  @Json("type") tpe: String,
  results: JValue
) extends ToJsonCapable
