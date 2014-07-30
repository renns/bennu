package com.qoid.bennu.client

import com.qoid.bennu.JsonAssist._
import m3.predef._
import m3.servlet.longpoll.ChannelId
import org.apache.http.client.methods._
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.HttpClients

import scala.concurrent._

object HttpAssist extends Logging {
  private lazy val httpClient = {
    val clientBuilder = HttpClients.custom()
    clientBuilder.setMaxConnPerRoute(1000)
    clientBuilder.setMaxConnTotal(1000)
    clientBuilder.build()
  }

  def httpPost(path: String, body: JValue): Future[String] = httpPost(path, body, None)

  def httpPost(path: String, body: JValue, channelId: ChannelId): Future[String] = httpPost(path, body, Some(channelId))

  private def httpPost(path: String, body: JValue, channelId: Option[ChannelId]): Future[String] = {
    val httpPost = new HttpPost(path)

    logger.debug(s"sending to ${path}\n--->\n${body.toJsonStr.indent("  ")}")

    channelId.foreach { id => httpPost.setHeader("Qoid-ChannelId", id.value) }
    httpPost.setHeader("Content-Type", "application/json")
    httpPost.setEntity(new StringEntity(body.toJsonStr))

    executeHttpRequest(httpPost)
  }

  private def executeHttpRequest(request: HttpUriRequest): Future[String] = {
    val p = Promise[String]()

    spawn("executeHttpRequest") {
      val response = httpClient.execute(request)

      if (response.getStatusLine.getStatusCode == 200) {
        val responseBody = response.getEntity.getContent.readString
        logger.debug(s"received from ${request.getURI.toString}\n<---\n${responseBody.indent("  ")}")
        p.success(responseBody)
      } else {
        p.failure(new Exception(s"Error calling '${request.getURI.toString}' -- ${response.getStatusLine.toString}"))
      }
    }

    p.future
  }
}
