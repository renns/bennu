package com.qoid.bennu.session

import com.qoid.bennu.JsonAssist._
import com.qoid.bennu.distributed.DistributedManager
import com.qoid.bennu.distributed.DistributedMessage
import com.qoid.bennu.distributed.DistributedMessageKind
import com.qoid.bennu.model.id.DistributedMessageId
import com.qoid.bennu.model.id.InternalId
import com.qoid.bennu.security.SecurityContext
import m3.predef._
import m3.servlet.beans.MultiRequestHandler.MethodInvocationResult
import m3.servlet.longpoll.ChannelId
import m3.servlet.longpoll.JettyChannelManager.JettyContinuationChannel
import scala.collection.mutable

class Session(injector: ScalaInjector, val securityContext: SecurityContext) extends Logging {
  private val standingQueries = mutable.HashMap.empty[JValue, List[(DistributedMessageId, List[InternalId])]]

  val channel: JettyContinuationChannel = createChannel()

  def put(result: MethodInvocationResult): Unit = {
    channel.put(serializer.toJson[MethodInvocationResult](result))
  }

  def addStandingQuery(context: JValue, messageId: DistributedMessageId, route: List[InternalId]): Unit = {
    standingQueries.synchronized{
      val values = standingQueries.getOrElse(context, List.empty[(DistributedMessageId, List[InternalId])])
      standingQueries.put(context, (messageId, route) :: values)
    }
  }

  def cancelStandingQuery(context: JValue): Unit = {
    val distributedMgr = injector.instance[DistributedManager]

    standingQueries.synchronized {
      for {
        values <- standingQueries.remove(context)
        (messageId, route) <- values
      } {
        distributedMgr.removeRequestData(messageId)

        val message = DistributedMessage(
          DistributedMessageKind.CancelQueryRequest,
          1,
          route,
          JNothing,
          Some(messageId)
        )

        distributedMgr.send(message)
      }
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
