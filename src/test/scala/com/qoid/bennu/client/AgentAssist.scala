package com.qoid.bennu.client

import com.qoid.bennu.JsonAssist._
import com.qoid.bennu.JsonAssist.jsondsl._
import com.qoid.bennu.client.HttpAssist.logger
import com.qoid.bennu.webservices.ServicePath
import m3.predef._
import scala.async.Async.async
import scala.async.Async.await
import scala.concurrent.ExecutionContext
import scala.concurrent.Future

object AgentAssist {

  def createAgent(
    agentName: String,
    password: String
  )(
    implicit
    config: HttpClientConfig,
    ec: ExecutionContext
  ): Future[String] = {

    async {
      val body = ("name" -> agentName) ~ ("password" -> password)
      val response = await(HttpAssist.httpPost(s"${config.server}${ServicePath.createAgent}", body))

      val authenticationId = parseJson(response) \ "authenticationId" match {
        case JString(value) => value
        case _ => m3x.error(s"Invalid create agent response -- ${response}")
      }

      logger.debug(s"Created agent with login '$authenticationId'")

      authenticationId
    }
  }

  def importAgent(agentData: JValue)(implicit config: HttpClientConfig, ec: ExecutionContext): Future[Unit] = {
    async {
      val body = "agentData" -> agentData
      await(HttpAssist.httpPost(s"${config.server}${ServicePath.importAgent}", body))
      logger.debug("Imported agent")
    }
  }

  def login(
    authenticationId: String,
    password: String
  )(
    implicit
    config: HttpClientConfig,
    ec: ExecutionContext
  ): Future[ChannelClient] = {
    async {
      val body = ("authenticationId" -> authenticationId) ~ ("password" -> password)
      val response = await(HttpAssist.httpPost(s"${config.server}${ServicePath.login}", body))
      val json = parseJson(response)
      val session = serializer.fromJson[Session](json)

      new HttpChannelClient(session.channelId, session.alias)
    }
  }
}
