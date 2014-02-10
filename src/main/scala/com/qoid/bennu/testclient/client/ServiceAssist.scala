package com.qoid.bennu.testclient.client

import com.qoid.bennu.JdbcAssist
import com.qoid.bennu.ServicePath
import com.qoid.bennu.model._
import com.qoid.bennu.squery.StandingQueryAction
import m3.json.LiftJsonAssist._

trait ServiceAssist {
  this: ChannelClient =>

  def upsert[T <: HasInternalId](instance: T): T = {
    val parms = Map("type" -> JString(instance.mapper.typeName), "instance" -> instance.toJson)

    val response = post(ServicePath.upsert, parms)

    response.result match {
      case JNothing => throw new Exception(s"Upsert didn't complete successfully")
      case r =>
        val mapper = JdbcAssist.findMapperByTypeName(instance.mapper.typeName).asInstanceOf[JdbcAssist.BennuMapperCompanion[HasInternalId]]
        mapper.fromJson(r).asInstanceOf[T]
    }
  }

  def delete[T <: HasInternalId](instance: T): T = {
    val parms = Map("type" -> JString(instance.mapper.typeName), "primaryKey" -> JString(instance.iid.value))

    val response = post(ServicePath.delete, parms)

    response.result match {
      case JNothing => throw new Exception(s"Delete didn't complete successfully")
      case r =>
        val mapper = JdbcAssist.findMapperByTypeName(instance.mapper.typeName).asInstanceOf[JdbcAssist.BennuMapperCompanion[HasInternalId]]
        mapper.fromJson(r).asInstanceOf[T]
    }
  }

  def registerStandingQuery(
    types: List[String]
  )(
    callback: (StandingQueryAction, InternalId, HasInternalId) => Unit
  ): InternalId = {

    val parms = Map("types" -> JArray(types.map(JString)))

    val response = post(ServicePath.registerStandingQuery, parms)

    response.result match {
      case JObject(JField("handle", JString(handle)) :: Nil) =>
        squeryCallbacks += InternalId(handle) -> callback
        InternalId(handle)
      case r => throw new Exception("Register standing query didn't complete successfully")
    }
  }

  def deRegisterStandingQuery(handle: InternalId): Boolean = {
    squeryCallbacks -= handle
    val parms = Map("handle" -> JString(handle.value))
    val response = post(ServicePath.deRegisterStandingQuery, parms)
    response.success
  }

  def sendNotification(toPeer: PeerId, kind: String, data: JValue): Boolean = {
    val parms = Map(
      "toPeer" -> JString(toPeer.value),
      "kind" -> JString(kind),
      "data" -> data
    )

    val response = post(ServicePath.sendNotification, parms)

    response.success
  }
}
