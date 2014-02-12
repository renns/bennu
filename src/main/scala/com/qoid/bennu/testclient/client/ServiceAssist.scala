package com.qoid.bennu.testclient.client

import com.qoid.bennu.JdbcAssist
import com.qoid.bennu.JsonAssist._
import com.qoid.bennu.ServicePath
import com.qoid.bennu.model._
import com.qoid.bennu.squery.StandingQueryAction

trait ServiceAssist {
  this: ChannelClient =>

  def upsert[T <: HasInternalId](instance: T): T = {
    val parms = Map("type" -> JString(instance.mapper.typeName), "instance" -> instance.toJson)

    val response = post(ServicePath.upsert, parms)

    response.result match {
      case JNothing => throw new Exception(s"Upsert didn't complete successfully")
      case r =>
        val mapper = JdbcAssist.findMapperByTypeName(instance.mapper.typeName)
        mapper.fromJson(r).asInstanceOf[T]
    }
  }

  def delete[T <: HasInternalId](instance: T): T = {
    val parms = Map("type" -> JString(instance.mapper.typeName), "primaryKey" -> JString(instance.iid.value))

    val response = post(ServicePath.delete, parms)

    response.result match {
      case JNothing => throw new Exception(s"Delete didn't complete successfully")
      case r =>
        val mapper = JdbcAssist.findMapperByTypeName(instance.mapper.typeName)
        mapper.fromJson(r).asInstanceOf[T]
    }
  }

  def query[T <: HasInternalId : Manifest](query: String): List[T] = {
    val typeName = manifest[T].runtimeClass.getSimpleName

    val parms = Map("type" -> JString(typeName), "q" -> JString(query))

    val response = post(ServicePath.query, parms)

    response.result match {
      case JNothing => throw new Exception("Query didn't complete successfully")
      case JArray(instances) =>
        val mapper = JdbcAssist.findMapperByTypeName(typeName)
        instances.map(mapper.fromJson(_).asInstanceOf[T])
      case _ => throw new Exception("Query returned invalid results")
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

  def initiateIntroduction(aConnection: Connection, aMessage: String, bConnection: Connection, bMessage: String): Boolean = {
    val parms = Map(
      "aConnectionIid" -> JString(aConnection.iid.value),
      "aMessage" -> JString(aMessage),
      "bConnectionIid" -> JString(bConnection.iid.value),
      "bMessage" -> JString(bMessage)
    )

    val response = post(ServicePath.initiateIntroduction, parms)

    response.success
  }

  def respondToIntroduction(notification: Notification, accepted: Boolean): Boolean = {
    val parms = Map(
      "notificationIid" -> JString(notification.iid.value),
      "accepted" -> JBool(accepted)
    )

    val response = post(ServicePath.respondToIntroduction, parms)

    response.success
  }

  def getProfiles(connections: List[Connection]): JValue = {
    val parms = Map(
      "connectionIids" -> JArray(connections.map(c => JString(c.iid.value)))
    )

    val response = post(ServicePath.getProfiles, parms)

    response.result
  }
}
