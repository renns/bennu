package com.qoid.bennu.testclient.client

import com.qoid.bennu.JdbcAssist
import com.qoid.bennu.ServicePath
import com.qoid.bennu.model._
import com.qoid.bennu.squery.StandingQueryAction
import com.qoid.bennu.testclient.client.HttpAssist.HttpClientConfig
import m3.Logging
import m3.json.LiftJsonAssist._
import m3.predef.m3x
import scala.concurrent._

object ServiceAssist extends HttpAssist with Logging {
  def createAgent(agentId: AgentId, overwrite: Boolean = true)(implicit config: HttpClientConfig): Unit = {
    val response = httpGet(s"${config.server}${ServicePath.createAgent}/${agentId.value}/${overwrite}")

    parseJson(response) \ "agentId" match {
      case JString(agentId.value) => logger.debug(s"Created agent ${agentId.value}")
      case _ => m3x.error(s"Invalid create agent response -- ${response}")
    }
  }
}

trait ServiceAssist {
  this: ChannelClient =>

  def upsert[T <: HasInternalId](instance: T)(implicit ec: ExecutionContext): Future[T] = {
    val parms = Map("type" -> JString(instance.mapper.typeName), "instance" -> instance.toJson)

    post(ServicePath.upsert, parms) map {
      _.result match {
        case JNothing => throw new Exception(s"Upsert didn't complete successfully")
        case r =>
          val mapper = JdbcAssist.findMapperByTypeName(instance.mapper.typeName).asInstanceOf[JdbcAssist.BennuMapperCompanion[HasInternalId]]
          mapper.fromJson(r).asInstanceOf[T]
      }
    }
  }

  def delete[T <: HasInternalId](instance: T)(implicit ec: ExecutionContext): Future[T] = {
    val parms = Map("type" -> JString(instance.mapper.typeName), "primaryKey" -> JString(instance.iid.value))

    post(ServicePath.delete, parms) map {
      _.result match {
        case JNothing => throw new Exception(s"Delete didn't complete successfully")
        case r =>
          val mapper = JdbcAssist.findMapperByTypeName(instance.mapper.typeName).asInstanceOf[JdbcAssist.BennuMapperCompanion[HasInternalId]]
          mapper.fromJson(r).asInstanceOf[T]
      }
    }
  }

  def registerStandingQuery(
    types: List[String]
  )(
    callback: (StandingQueryAction, InternalId, HasInternalId) => Unit
  )(
    implicit ec: ExecutionContext
  ): Future[InternalId] = {

    val parms = Map("types" -> JArray(types.map(JString)))

    post(ServicePath.registerStandingQuery, parms) map {
      _.result match {
        case JObject(JField("handle", JString(handle)) :: Nil) =>
          squeryCallbacks += InternalId(handle) -> callback
          InternalId(handle)
        case r => throw new Exception("Register standing query didn't complete successfully")
      }
    }
  }

  def deRegisterStandingQuery(handle: InternalId)(implicit ec: ExecutionContext): Future[Boolean] = {
    squeryCallbacks -= handle
    val parms = Map("handle" -> JString(handle.value))
    post(ServicePath.deRegisterStandingQuery, parms).map(_.success)
  }

  def sendNotification(toPeer: PeerId, kind: String, data: JValue)(implicit ec: ExecutionContext): Future[Boolean] = {
    val parms = Map(
      "toPeer" -> JString(toPeer.value),
      "kind" -> JString(kind),
      "data" -> data
    )

    post(ServicePath.sendNotification, parms).map(_.success)
  }
}
