package com.qoid.bennu.client

import com.qoid.bennu.JsonAssist._
import com.qoid.bennu.JsonAssist.jsondsl._
import com.qoid.bennu.ServicePath
import com.qoid.bennu.model.id.InternalId
import m3.predef._
import org.apache.http.client.methods._
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.HttpClients
import scala.async.Async._
import scala.concurrent._

object HttpAssist extends HttpAssist with Logging {
  def createAgent()(implicit config: HttpClientConfig, ec: ExecutionContext): Future[ChannelClient] = {
    async {
      val agentName = InternalId.uidGenerator.create(32)

      val createAgentBody = "name" -> agentName
      val response = await(httpPost(s"${config.server}${ServicePath.createAgent}", createAgentBody, None))

      parseJson(response) \ "agentName" match {
        case JString(n) => logger.debug(s"Created agent $n")
        case _ => m3x.error(s"Invalid create agent response -- ${response}")
      }

      await(ChannelClientFactory.createHttpChannelClient(agentName, Some("Anonymous")))
    }
  }

  def importAgent(agentData: JValue)(implicit config: HttpClientConfig, ec: ExecutionContext): Future[Unit] = {
    async {
      val response = await(httpPost(s"${config.server}${ServicePath.importAgent}", "agentData" -> agentData, None))

      parseJson(response) match {
        case JString("success") => logger.debug("Imported agent")
        case _ => m3x.error(s"Invalid import agent response -- ${response}")
      }
    }
  }
}

trait HttpAssist { self: Logging =>

  private lazy val httpClient = {
    val clientBuilder = HttpClients.custom()
    clientBuilder.setMaxConnPerRoute(1000)
    clientBuilder.setMaxConnTotal(1000)
    clientBuilder.build()
  }

  protected def httpGet(path: String): Future[String] = {
    executeHttpRequest(new HttpGet(path))
  }

  protected def httpPost(path: String, body: JValue, cookie: Option[String]): Future[String] = {
    val httpPost = new HttpPost(path)

    logger.debug(s"sending to ${path} \n  ---> \n${body.toJsonStr.indent("        ")}")

    cookie.foreach(httpPost.setHeader("Cookie", _))
    httpPost.setHeader("Content-Type", "application/json")
    httpPost.setEntity(new StringEntity(body.toJsonStr))

    executeHttpRequest(httpPost)
  }

  private def executeHttpRequest(request: HttpUriRequest): Future[String] = {
    val p = Promise[String]()

    spawn("executeHttpRequest") {
      val response = httpClient.execute(request)

      if (response.getStatusLine.getStatusCode == 200) {
        p.success(response.getEntity.getContent.readString)
      } else {
        p.failure(new Exception(s"Error calling '${request.getURI.toString}' -- ${response.getStatusLine.toString}"))
      }
    }

    p.future
  }
}
