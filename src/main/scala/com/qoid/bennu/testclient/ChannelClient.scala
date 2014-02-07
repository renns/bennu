package com.qoid.bennu.testclient

import com.qoid.bennu.JsonCapable
import com.qoid.bennu.model._
import m3.json.LiftJsonAssist._
import m3.predef._
import m3.servlet.longpoll.ChannelId
import net.liftweb.json.JString
import org.apache.http.client.methods._
import org.apache.http.impl.client.HttpClientBuilder
import scala.concurrent._
import com.qoid.bennu.model.Alias
import scala.collection.immutable.HashMap
import scala.concurrent.ExecutionContext.Implicits.global

trait ChannelClient {

  def agentId: AgentId
  
  def post(path: String, parms: Map[String, JValue]): Future[ChannelResponse]
  def registerStandingQuery(types: List[String])(callback: (InternalId, HasInternalId) => Unit): Future[InternalId]
  def deRegisterStandingQuery(handle: InternalId): Future[Boolean]
  
  def createAlias(aliasName: String): (Future[Alias],Future[Label]) = {
    
    val aliasIid = InternalId.random
    val rootLabelIid = InternalId.random
    
    // create alias here
    val aliasFuture = upsert(Alias(
        iid = aliasIid, 
        agentId = agentId,
        rootLabelIid = rootLabelIid,
        name = aliasName,
        data = JNothing
    ))

    // create alias here
    val rootLabelFuture = upsert(Label(
        iid = rootLabelIid, 
        agentId = agentId,
        name = aliasName,
        data = JNothing
    ))

    aliasFuture -> rootLabelFuture
    
  }

  def upsert[T <: HasInternalId](v: T): Future[T] = {
    post(ApiPath.upsert, HashMap("type" -> JString(v.mapper.typeName), "instance" -> v.toJson)).map(_ => v)
  }
  
}

object AgentManager extends Logging {
  def createAgent(agentId: AgentId, overwrite: Boolean = true): Unit = {
    val host = "http://localhost:8080"

    val client = HttpClientBuilder.create.build
    val httpGet = new HttpGet(s"$host${ApiPath.createAgent}/${agentId.value}/${overwrite}")
    val response = client.execute(httpGet)
    val responseBody = response.getEntity.getContent.readString

    parseJson(responseBody) \ "agentId" match {
      case JString(agentId.value) => logger.debug(s"Created agent ${agentId.value}")
      case _ => m3x.error(s"don't know how to handle create agent response -- ${responseBody}")
    }
  }
}

object ChannelClientFactory {
  def createHttpChannelClient(agentId: AgentId): HttpChannelClient with SendNotification = {
    val host = "http://localhost:8080"

    val client = HttpClientBuilder.create.build
    val httpGet = new HttpGet(s"$host${ApiPath.createChannel}/${agentId.value}")
    val response = client.execute(httpGet)
    val responseBody = response.getEntity.getContent.readString

    val channelId = parseJson(responseBody) \ "id" match {
      case JString(id) => ChannelId(id)
      case _ => m3x.error(s"don't know how to handle create channel response -- ${responseBody}")
    }

    new HttpChannelClient(host, agentId, channelId) with SendNotification
  }
}

object ApiPath {
  val createAgent = "/api/agent/create"
  val createChannel = "/api/channel/create"
  val submitChannel = "/api/channel/submit"
  val pollChannel = "/api/channel/poll"
  val upsert = "/api/upsert"
  val delete = "/api/delete"
  val query = "/api/query"
  val registerStandingQuery = "/api/squery/register"
  val deRegisterStandingQuery = "/api/squery/deregister"
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
