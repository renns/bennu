package com.qoid.bennu.session

import com.qoid.bennu.JsonAssist._
import com.qoid.bennu.distributed.DistributedManager
import com.qoid.bennu.model.id.DistributedMessageId
import com.qoid.bennu.security.SecurityContext
import m3.LockFreeMap
import m3.predef._
import m3.servlet.beans.MultiRequestHandler.MethodInvocationResult
import m3.servlet.longpoll.ChannelId
import m3.servlet.longpoll.JettyChannelManager.JettyContinuationChannel

class Session(injector: ScalaInjector, val securityContext: SecurityContext) extends Logging {
  private val standingQueries = LockFreeMap.empty[JValue, DistributedMessageId]

  val channel: JettyContinuationChannel = createChannel()

  def put(result: MethodInvocationResult): Unit = {
    channel.put(serializer.toJson[MethodInvocationResult](result))
  }

  def addStandingQuery(context: JValue, messageId: DistributedMessageId): Unit = {
    standingQueries.put(context, messageId)
  }

  def cancelStandingQuery(context: JValue): Unit = {
    val distributedMgr = injector.instance[DistributedManager]
    standingQueries.remove(context).foreach(distributedMgr.removeRequestData)
  }

  def close(): Unit = {
    standingQueries.keys.foreach(cancelStandingQuery)
  }

  private def createChannel(): JettyContinuationChannel = {
    val channelId = ChannelId.random()
    logger.debug(s"creating channel ${channelId.value}")
    JettyContinuationChannel(channelId)
  }
}
