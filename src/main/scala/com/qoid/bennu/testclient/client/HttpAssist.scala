package com.qoid.bennu.testclient.client

import m3.json.LiftJsonAssist._
import m3.predef._
import net.model3.lang.TimeDuration
import org.apache.http.client.methods._
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.HttpClientBuilder

object HttpAssist {
  case class HttpClientConfig(
    server: String = "http://localhost:8080",
    pollTimeout: TimeDuration = new TimeDuration("10 seconds")
  )
}

trait HttpAssist {
  protected def httpGet(path: String): String = {
    executeHttpRequest(new HttpGet(path))
  }

  protected def httpPost(path: String, body: JValue, cookie: Option[String]): String = {
    val httpPost = new HttpPost(path)

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
