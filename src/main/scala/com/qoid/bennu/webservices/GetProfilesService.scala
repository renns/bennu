package com.qoid.bennu.webservices

import com.google.inject.Inject
import com.qoid.bennu.ErrorCode
import com.qoid.bennu.JsonAssist._
import com.qoid.bennu.JsonAssist.jsondsl._
import com.qoid.bennu.ToJsonCapable
import com.qoid.bennu.distributed.DistributedManager
import com.qoid.bennu.distributed.DistributedRequestKind
import com.qoid.bennu.model._
import java.sql.{ Connection => JdbcConn }
import m3.predef._
import m3.servlet.beans.Parm
import m3.servlet.longpoll.ChannelId
import net.model3.lang.TimeDuration
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.TimeoutException
import scala.util.Failure
import scala.util.Success
import m3.servlet.beans.MultiRequestHandler.MethodInvocationContext

case class GetProfilesService @Inject() (
  implicit
  jdbcConn: JdbcConn,
  distributedMgr: DistributedManager,
  channelId: ChannelId,
  miContext: Option[MethodInvocationContext],
  @Parm connectionIids: List[InternalId],
  @Parm timeout: Int = 5000
) extends Logging {

  val resolvedContext = miContext.map(_.value).getOrElse(JNothing)
  
  def service: JValue = {
    val handle = InternalId.random

    for (connectionIid <- connectionIids) {
      distributedMgr.sendRequest(
        connectionIid,
        DistributedRequestKind.GetProfile,
        JNothing,
        new TimeDuration(timeout)
      ).onComplete {
        case Success(profile) =>
          val data = GetProfilesService.ResponseData(connectionIid, Some(profile))
          AsyncResponse(AsyncResponseType.Profile, handle, true, data.toJson, context = resolvedContext).send(channelId)
        case Failure(t: TimeoutException) =>
          val data = GetProfilesService.ResponseData(connectionIid)
          val error = AsyncResponseError(ErrorCode.Timeout, t.getMessage)
          AsyncResponse(AsyncResponseType.Profile, handle, false, data.toJson, context = resolvedContext, Some(error)).send(channelId)
          logger.debug(s"get profiles: timed out after $timeout milliseconds")
        case Failure(t) =>
          val data = GetProfilesService.ResponseData(connectionIid)
          val error = AsyncResponseError(ErrorCode.Generic, t.getMessage, Some(t.getStackTraceString))
          AsyncResponse(AsyncResponseType.Profile, handle, false, data.toJson, context = resolvedContext, Some(error)).send(channelId)
          logger.warn("get profiles: FAIL", t)
      }
    }

    "handle" -> handle.value
  }
}

object GetProfilesService {
  case class ResponseData(
    connectionIid: InternalId,
    profile: Option[JValue] = None
  ) extends ToJsonCapable
}
