package com.qoid.bennu.session

import com.qoid.bennu.JsonAssist._
import com.qoid.bennu.model.id.DistributedMessageId
import com.qoid.bennu.model.id.InternalId
import com.qoid.bennu.security.SecurityContext
import com.qoid.bennu.query.QueryManager
import m3.LockFreeMap
import m3.predef._
import m3.servlet.longpoll.ChannelId
import m3.servlet.longpoll.JettyChannelManager.JettyContinuationChannel

class Session(injector: ScalaInjector, val securityContext: SecurityContext) extends Logging {
  private val standingQueries = LockFreeMap.empty[JValue, (DistributedMessageId, List[InternalId])]

  val channel: JettyContinuationChannel = createChannel()

  def addStandingQuery(context: JValue, messageId: DistributedMessageId, route: List[InternalId]): Unit = {
    standingQueries.put(context, (messageId, route))
  }

  def cancelStandingQuery(context: JValue): Unit = {
    val queryMgr = injector.instance[QueryManager]

    for ((messageId, route) <- standingQueries.remove(context)) {
      queryMgr.cancelQuery(messageId, route)
    }
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
