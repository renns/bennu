package com.qoid.bennu.client

import com.qoid.bennu.JsonAssist._
import com.qoid.bennu.JsonAssist.jsondsl._
import com.qoid.bennu.ServicePath
import com.qoid.bennu.model.id.InternalId
import m3.predef._
import m3.servlet.longpoll.ChannelId
import scala.async.Async._
import scala.concurrent._

object ChannelClientFactory extends HttpAssist with Logging {
  def createHttpChannelClient(
    agentName: String,
    aliasName: Option[String] = None,
    password: String = "password"
  )(
    implicit
    config: HttpClientConfig,
    ec: ExecutionContext
  ): Future[HttpChannelClient] = {

    async {
      val authenticationId = agentName + aliasName.map("." + _).mkString("")

      val loginBody = ("authenticationId" -> authenticationId) ~ ("password" -> password)
      val response = await(httpPost(s"${config.server}${ServicePath.login}", loginBody, None))

      val channelId = parseJson(response) \ "channelId" match {
        case JString(id) => ChannelId(id)
        case _ => m3x.error(s"Invalid create channel response -- ${response}")
      }

      val aliasIid = parseJson(response) \ "aliasIid" match {
        case JString(iid) => InternalId(iid)
        case _ => m3x.error(s"Invalid create channel response -- ${response}")
      }

      new HttpChannelClient(agentName, channelId, aliasIid)
    }
  }
}
