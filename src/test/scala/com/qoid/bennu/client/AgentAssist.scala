package com.qoid.bennu.client

import com.qoid.bennu.JsonAssist._
import com.qoid.bennu.JsonAssist.jsondsl._
import com.qoid.bennu.ServicePath
import com.qoid.bennu.client.HttpAssist.logger
import com.qoid.bennu.model.id.InternalId
import m3.predef._
import m3.servlet.longpoll.ChannelId

import scala.async.Async.async
import scala.async.Async.await
import scala.concurrent.ExecutionContext
import scala.concurrent.Future

object AgentAssist {

  //  val agentName = InternalId.uidGenerator.create(32)
  //  val password = "test"

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

      val channelId = parseJson(response) \ "channelId" match {
        case JString(id) => ChannelId(id)
        case _ => m3x.error(s"Invalid create channel response -- ${response}")
      }

      val connectionIid = parseJson(response) \ "connectionIid" match {
        case JString(id) => InternalId(id)
        case _ => m3x.error(s"Invalid create channel response -- ${response}")
      }

      new HttpChannelClient(channelId, connectionIid)
    }
  }
}
