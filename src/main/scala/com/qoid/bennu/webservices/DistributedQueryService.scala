package com.qoid.bennu.webservices

import com.google.inject.Inject
import com.qoid.bennu.ErrorCode
import com.qoid.bennu.JsonAssist._
import com.qoid.bennu.JsonAssist.jsondsl._
import com.qoid.bennu.SecurityContext.AgentCapableSecurityContext
import com.qoid.bennu.ToJsonCapable
import com.qoid.bennu.distributed.DistributedManager
import com.qoid.bennu.distributed.DistributedRequestKind
import com.qoid.bennu.model._
import com.qoid.bennu.squery.StandingQueryManager2
import java.sql.{ Connection => JdbcConn }
import m3.json.Json
import m3.predef._
import m3.servlet.beans.Parm
import m3.servlet.longpoll.ChannelId
import net.model3.lang.TimeDuration
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.TimeoutException
import scala.util.Failure
import scala.util.Success

case class DistributedQueryService @Inject()(
  implicit
  jdbcConn: JdbcConn,
  distributedMgr: DistributedManager,
  sQueryMgr: StandingQueryManager2,
  securityContext: AgentCapableSecurityContext,
  channelId: ChannelId,
  @Parm("type") _type: String,
  @Parm("q") queryStr: String,
  @Parm connectionIids: List[InternalId],
  @Parm leaveStanding: Boolean = false,
  @Parm timeout: Int = 5000
) extends Logging {

  def service: JValue = {
    val handle = InternalId.random

    for (connectionIid <- connectionIids) {
      if (leaveStanding) {
        sQueryMgr.add(securityContext.agentId, handle, connectionIid, channelId, _type)
      }

      distributedMgr.sendRequest(
        connectionIid,
        DistributedRequestKind.Query,
        DistributedQueryService.RequestData(_type, queryStr, leaveStanding, handle).toJson,
        new TimeDuration(timeout)
      ).onComplete {
        case Success(results) =>
          val data = DistributedQueryService.ResponseData(connectionIid, _type, Some(results))
          AsyncResponse(AsyncResponseType.Query, handle, true, data.toJson).send(channelId)
        case Failure(t: TimeoutException) =>
          val data = DistributedQueryService.ResponseData(connectionIid, _type)
          AsyncResponse(AsyncResponseType.Query, handle, false, data.toJson, Some(ErrorCode.Timeout), Some(t.getMessage)).send(channelId)
          logger.debug(s"distributed query: timed out after $timeout milliseconds")
        case Failure(t) =>
          val data = DistributedQueryService.ResponseData(connectionIid, _type)
          AsyncResponse(AsyncResponseType.Query, handle, false, data.toJson, Some(ErrorCode.Generic), Some(t.getMessage), Some(t.getStackTraceString)).send(channelId)
          logger.warn("distributed query: FAIL", t)
      }
    }

    "handle" -> handle
  }
}

object DistributedQueryService {
  case class RequestData(
    @Json("type") tpe: String,
    query: String,
    leaveStanding: Boolean,
    handle: InternalId
  ) extends ToJsonCapable

  case class ResponseData(
    connectionIid: InternalId,
    @Json("type") tpe: String,
    results: Option[JValue] = None
  ) extends ToJsonCapable
}
