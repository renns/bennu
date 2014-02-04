package com.qoid.bennu.testclient

import m3.servlet.longpoll.ChannelId
import net.liftweb.json.JValue
import org.apache.http.client.HttpClient
import org.apache.http.impl.client.DefaultHttpClient
import net.model3.lang.TimeDuration
import org.apache.http.client.methods.HttpGet
import com.github.theon.uri.Uri
import m3.predef._
import m3.json.LiftJsonAssist._
import com.qoid.bennu.model.AgentId
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.client.methods.HttpPost
import com.qoid.bennu.model.InternalId
import m3.json.LiftJsonAssist._
import jsondsl._
import org.apache.http.entity.StringEntity

object HttpAssist {

  case class HttpClientConfig(
      server: String = "http://localhost:8080",
      pollTimeout: TimeDuration = new TimeDuration("30 seconds")
  )
  
}

trait HttpAssist extends Logging {
  
  import HttpAssist._
  
  def client = HttpClientBuilder.create.build
  
  def spawnLongPoller(implicit id: ChannelId, config: HttpClientConfig) = spawn(s"long-poller-${id.value}"){
    while( true ) {
      longPoll.foreach { msg =>
        logger.debug(s"long poll response\n${msg.toJsonStr}")
      }
    }
  }
  
  def longPoll(implicit id: ChannelId, config: HttpClientConfig): List[JValue] = {
    
    val get = new HttpGet((config.server + "/api/channel/poll/" + id.value + "/" + config.pollTimeout.inMilliseconds).toString)
   
    val response = client.execute(get)
    
    val responseBody = response.getEntity.getContent.readString
    
    parseJson(responseBody) match {
      case JNothing => Nil
      case JArray(messages) => messages
      case jv => m3x.error(s"don't know how to handle -- ${responseBody}")
    }
    
  }
  
  def createChannel(implicit agentId: AgentId, config: HttpClientConfig): ChannelId = {
    
    val get = new HttpGet(config.server + "/api/channel/create/" + agentId.value)
   
    val response = client.execute(get)
    
    val responseBody = response.getEntity.getContent.readString
    
    (parseJson(responseBody) \ "id") match {
      case JString(id) => ChannelId(id)
      case _ => m3x.error(s"don't know how to handle create channel response -- ${responseBody}")
    }
    
  }

  def registerStandingQuery(types: List[String])(implicit channel: ChannelId, config: HttpClientConfig): InternalId = {
    
    val handle = InternalId.random
    
    post(
      path = "/api/squery/register",
      channel = Some(channel),
      jsonBody = ("handle" -> handle.value) ~ ("types" -> types)
    )
    
    handle
    
  }
  
  def post(path: String, channel: Option[ChannelId] = None, jsonBody: JValue)(implicit config: HttpClientConfig) = {

    val post = new HttpPost(config.server + path)
   
    val cl = client
    
    channel.foreach { ch =>
      post.setHeader("Cookie", s"channel=${ch.value}")
    }
    
    post.setHeader("Content-Type", "application/json")
    
    post.setEntity(new StringEntity(jsonBody.toJsonStr))
    
    val response = cl.execute(post)
    
    response.getEntity.getContent.readString
    
  }
  
  
}
