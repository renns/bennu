package com.qoid.bennu.testclient.client

import com.qoid.bennu.ServicePath
import com.qoid.bennu.ToJsonCapable
import com.qoid.bennu.model._
import com.qoid.bennu.squery.StandingQueryAction
import com.qoid.bennu.testclient.client.HttpAssist.HttpClientConfig
import m3.LockFreeMap
import m3.json.LiftJsonAssist._
import m3.predef._
import m3.servlet.longpoll.ChannelId
import net.liftweb.json.JString
import scala.concurrent._

trait ChannelClient extends ServiceAssist with ModelAssist {
  def agentId: AgentId

  protected val squeryCallbacks = new LockFreeMap[InternalId, (StandingQueryAction, InternalId, HasInternalId) => Unit]
  protected val asyncCallbacks = new LockFreeMap[InternalId, AsyncResponse => Unit]

  def postAsync(path: String, parms: Map[String, JValue], context: JValue = JNothing)(implicit ec: ExecutionContext): Future[ChannelResponse]
  def post(path: String, parms: Map[String, JValue], context: JValue = JNothing): ChannelResponse
}

object ChannelClientFactory extends HttpAssist with Logging {
  def createHttpChannelClient(
    agentId: AgentId
  )(
    implicit config: HttpClientConfig
  ): HttpChannelClient = {

    val response = httpGet(s"${config.server}${ServicePath.createChannel}/${agentId.value}")

    val channelId = parseJson(response) \ "id" match {
      case JString(id) => ChannelId(id)
      case _ => m3x.error(s"Invalid create channel response -- ${response}")
    }

    new HttpChannelClient(agentId, channelId)
  }
}

case class ChannelRequest(
  channel: ChannelId,
  requests: List[ChannelRequestRequest]
) extends ToJsonCapable

case class ChannelRequestRequest(
  path: String,
  context: JValue,
  parms: JValue
)

case class ChannelResponse(
  handle: Option[String],
  data: JValue,
  responseType: Option[String],
  success: Boolean,
  context: JValue,
  result: JValue,
  error: Option[ChannelResponseError]
)

case class ChannelResponseError(
  message: Option[String],
  stacktrace: String
)
