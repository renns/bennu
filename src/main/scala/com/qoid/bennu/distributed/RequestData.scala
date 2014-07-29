package com.qoid.bennu.distributed

import com.qoid.bennu.FromJsonCapable
import com.qoid.bennu.ToJsonCapable
import m3.servlet.longpoll.ChannelId
import net.liftweb.json.JValue

object RequestData extends FromJsonCapable[RequestData]

case class RequestData(
  channelId: ChannelId,
  context: JValue,
  singleResponse: Boolean
) extends ToJsonCapable
