package com.qoid.bennu.distributed.messages

import com.qoid.bennu.FromJsonCapable
import com.qoid.bennu.JsonAssist._
import com.qoid.bennu.ToJsonCapable
import com.qoid.bennu.query.QueryManager
import com.qoid.bennu.query.StandingQueryAction
import m3.json.Json
import m3.predef._

object StandingQueryResponse extends FromJsonCapable[StandingQueryResponse] with Logging {
  def handle(message: DistributedMessage, injector: ScalaInjector): Unit = {
    (message.kind, message.version) match {
      case (DistributedMessageKind.StandingQueryResponse, 1) =>
        val queryMgr = injector.instance[QueryManager]
        val standingQueryResponse = fromJson(message.data)

        //TODO: validate (replyToMessageId must be set)

        queryMgr.handleStandingQueryResponse(message.replyToMessageId.get, message.replyRoute, standingQueryResponse)

      case (kind, version) => logger.warn(s"unsupported distributed message -- ${kind} v${version}")
    }
  }
}

case class StandingQueryResponse(
  @Json("type") tpe: String,
  results: JValue,
  action: StandingQueryAction
) extends ToJsonCapable
