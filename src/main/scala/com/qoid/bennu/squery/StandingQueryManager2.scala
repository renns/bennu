package com.qoid.bennu.squery

import com.google.inject.Inject
import com.qoid.bennu.JsonAssist._
import com.qoid.bennu.ToJsonCapable
import com.qoid.bennu.model.AgentId
import com.qoid.bennu.model.AsyncResponse
import com.qoid.bennu.model.AsyncResponseType
import com.qoid.bennu.model.InternalId
import m3.LockFreeMap
import m3.json.Json
import m3.servlet.longpoll.ChannelId
import m3.servlet.longpoll.ChannelManager

@com.google.inject.Singleton
class StandingQueryManager2 @Inject() (channelMgr: ChannelManager) {
  
  private val map = new LockFreeMap[StandingQueryManager2.MapKey, StandingQueryManager2.MapValue]

  def add(agentId: AgentId, handle: InternalId, connectionIid: InternalId, channelId: ChannelId, tpe: String): Unit = {
    map.put(
      StandingQueryManager2.MapKey(agentId, handle, connectionIid),
      StandingQueryManager2.MapValue(channelId, tpe)
    )
  }

  def remove(agentId: AgentId, handle: InternalId, connectionIid: InternalId): Unit = {
    map.remove(StandingQueryManager2.MapKey(agentId, handle, connectionIid))
  }

  def notify(
    agentId: AgentId,
    handle: InternalId,
    connectionIid: InternalId,
    action: StandingQueryAction,
    data: JValue
  ): Unit = {
    map.get(StandingQueryManager2.MapKey(agentId, handle, connectionIid)).foreach { v =>
      val responseData = StandingQueryManager2.ResponseData(connectionIid, action, v.tpe, data)
      val response = AsyncResponse(AsyncResponseType.SQuery2, handle, true, responseData.toJson)
      val channel = channelMgr.channel(v.channelId)
      channel.put(response.toJson)
    }
  }
}

object StandingQueryManager2 {
  case class MapKey(
    agentId: AgentId,
    handle: InternalId,
    connectionIid: InternalId
  )

  case class MapValue(
    channelId: ChannelId,
    tpe: String
  )

  case class ResponseData(
    connectionIid: InternalId,
    action: StandingQueryAction,
    @Json("type") tpe: String,
    data: JValue
  ) extends ToJsonCapable
}
