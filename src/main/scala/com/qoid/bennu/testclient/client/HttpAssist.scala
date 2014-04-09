package com.qoid.bennu.testclient.client

import com.qoid.bennu.JsonAssist._
import com.qoid.bennu.JsonAssist.jsondsl._
import com.qoid.bennu.ServicePath
import m3.predef._
import net.model3.lang.TimeDuration
import org.apache.http.client.methods._
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.HttpClientBuilder

object HttpAssist extends HttpAssist with Logging {
  case class HttpClientConfig(
    server: String = "http://localhost:8080",
    pollTimeout: TimeDuration = new TimeDuration("10 seconds"),
    requestTimeout: TimeDuration = new TimeDuration("30 seconds")
  )

  def createAgent(agentName: String)(implicit config: HttpClientConfig): ChannelClient = {
    try {
      val client = ChannelClientFactory.createHttpChannelClient(agentName)
      client.deleteAgent(false)
    } catch {
      case _: Exception =>
    }

    val response = httpGet(s"${config.server}${ServicePath.createAgent}/$agentName")

    parseJson(response) \ "agentName" match {
      case JString(n) => logger.debug(s"Created agent $n")
      case _ => m3x.error(s"Invalid create agent response -- ${response}")
    }

    ChannelClientFactory.createHttpChannelClient(agentName)
  }

  def importAgent(agentData: JValue)(implicit config: HttpClientConfig): Unit = {
    val response = httpPost(s"${config.server}${ServicePath.importAgent}", "agentData" -> agentData, None)

    parseJson(response) match {
      case JString("success") => logger.debug("Imported agent")
      case _ => m3x.error(s"Invalid import agent response -- ${response}")
    }
  }
}

trait HttpAssist { self: Logging =>
  protected def httpGet(path: String): String = {
    executeHttpRequest(new HttpGet(path))
  }

  protected def httpPost(path: String, body: JValue, cookie: Option[String]): String = {
    val httpPost = new HttpPost(path)

    logger.debug(s"sending to ${path} \n  ---> \n${body.toJsonStr.indent("        ")}")

    cookie.foreach(httpPost.setHeader("Cookie", _))
    httpPost.setHeader("Content-Type", "application/json")
    httpPost.setEntity(new StringEntity(body.toJsonStr))

    executeHttpRequest(httpPost)
  }

  private def executeHttpRequest(request: HttpUriRequest): String = {
    val client = HttpClientBuilder.create.build
    val response = client.execute(request)

    if (response.getStatusLine.getStatusCode != 200) {
      m3x.error(s"Error calling '${request.getURI.toString}' -- ${response.getStatusLine.toString}")
    }

    response.getEntity.getContent.readString
  }
}
