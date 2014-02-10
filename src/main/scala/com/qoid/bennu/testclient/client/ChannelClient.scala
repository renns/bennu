package com.qoid.bennu.testclient.client

import com.qoid.bennu.JsonCapable
import com.qoid.bennu.ServicePath
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

  def postAsync(path: String, parms: Map[String, JValue])(implicit ec: ExecutionContext): Future[ChannelResponse]
  def post(path: String, parms: Map[String, JValue]): ChannelResponse
}

object ChannelClientFactory extends HttpAssist {
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
) extends JsonCapable

case class ChannelRequestRequest(
  path: String,
  context: String,
  parms: JValue
)

case class ChannelResponse(
  success: Boolean,
  context: String,
  result: JValue,
  error: Option[ChannelResponseError]
)

case class ChannelResponseError(
  message: Option[String],
  stacktrace: String
)
