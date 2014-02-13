package com.qoid.bennu.webservices

import com.google.inject.Inject
import com.qoid.bennu.JsonAssist._
import com.qoid.bennu.JsonAssist.jsondsl._
import com.qoid.bennu.distributed.DistributedManager
import com.qoid.bennu.distributed.DistributedRequestKind
import com.qoid.bennu.model._
import java.sql.{ Connection => JdbcConn }
import m3.predef._
import m3.servlet.beans.Parm
import m3.servlet.longpoll.ChannelId
import m3.servlet.longpoll.ChannelManager
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Failure
import scala.util.Success

case class GetProfilesService @Inject() (
  implicit
  jdbcConn: JdbcConn,
  distributedMgr: DistributedManager,
  channelMgr: ChannelManager,
  channelId: ChannelId,
  @Parm connectionIids: List[InternalId]
) extends Logging {
  def service: JValue = {
    val handle = InternalId.random

    for (connectionIid <- connectionIids) {
      distributedMgr.sendRequest(connectionIid, DistributedRequestKind.GetProfile, JNothing).onComplete {
        case Success(data) =>
          val response = AsyncResponse(AsyncResponseType.Profile, handle, data)
          val channel = channelMgr.channel(channelId)
          channel.put(response.toJson)
        case Failure(t) => logger.warn(t)
      }
    }

    "handle" -> handle.value
  }
}
