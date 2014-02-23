package com.qoid.bennu.testclient.client

import com.qoid.bennu.JdbcAssist
import com.qoid.bennu.JsonAssist._
import com.qoid.bennu.JsonAssist.jsondsl._
import com.qoid.bennu.ServicePath
import com.qoid.bennu.model._
import com.qoid.bennu.squery.StandingQueryAction

trait ServiceAssist {
  this: ChannelClient =>

  def upsert[T <: HasInternalId](instance: T): T = {
    val parms = Map[String, JValue]("type" -> instance.mapper.typeName, "instance" -> instance.toJson)

    val response = post(ServicePath.upsert, parms)

    response.result match {
      case JNothing => throw new Exception(s"Upsert didn't complete successfully")
      case r =>
        val mapper = JdbcAssist.findMapperByTypeName(instance.mapper.typeName)
        mapper.fromJson(r).asInstanceOf[T]
    }
  }

  def delete[T <: HasInternalId](instance: T): T = {
    val parms = Map[String, JValue]("type" -> instance.mapper.typeName, "primaryKey" -> instance.iid)

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

    val parms = Map[String, JValue]("type" -> typeName, "q" -> query)

    val response = post(ServicePath.query, parms)

    response.result match {
      case JNothing => throw new Exception("Query didn't complete successfully")
      case JArray(instances) =>
        val mapper = JdbcAssist.findMapperByTypeName(typeName)
        instances.map(mapper.fromJson(_).asInstanceOf[T])
      case _ => throw new Exception("Query returned invalid results")
    }
  }

  def distributedQuery[T <: HasInternalId : Manifest](
    query: String,
    aliases: List[Alias],
    connections: List[Connection],
    timeout: String = "5 seconds",
    context: JValue = JNothing
  )(
    callback: AsyncResponse => Unit
  ): InternalId = {

    val typeName = manifest[T].runtimeClass.getSimpleName

    val parms = Map[String, JValue](
      "type" -> typeName,
      "q" -> query,
      "aliasIids" -> aliases.map(_.iid),
      "connectionIids" -> connections.map(c => c.iid),
      "leaveStanding" -> false,
      "timeout" -> timeout,
      "context" -> context
    )

    val response = post(ServicePath.distributedQuery, parms)

    response.result match {
      case JObject(JField("handle", JString(handle)) :: Nil) =>
        asyncCallbacks += InternalId(handle) -> callback
        InternalId(handle)
      case r => throw new Exception(s"Distributed query result invalid -- $r")
    }
  }

  def registerStandingQuery(
    types: List[String]
  )(
    callback: (StandingQueryAction, InternalId, HasInternalId) => Unit
  ): InternalId = {

    val parms = Map[String, JValue]("types" -> types)

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
    val parms = Map[String, JValue]("handle" -> handle)
    val response = post(ServicePath.deRegisterStandingQuery, parms)
    response.success
  }

  def sendNotification(connectionIid: InternalId, kind: NotificationKind, data: JValue): Boolean = {
    val parms = Map[String, JValue](
      "connectionIid" -> connectionIid,
      "kind" -> kind.toString,
      "data" -> data
    )

    val response = post(ServicePath.sendNotification, parms)

    response.success
  }

  def initiateIntroduction(aConnection: Connection, aMessage: String, bConnection: Connection, bMessage: String): Boolean = {
    val parms = Map[String, JValue](
      "aConnectionIid" -> aConnection.iid,
      "aMessage" -> aMessage,
      "bConnectionIid" -> bConnection.iid,
      "bMessage" -> bMessage
    )

    val response = post(ServicePath.initiateIntroduction, parms)

    response.success
  }

  def respondToIntroduction(notification: Notification, accepted: Boolean): Boolean = {
    val parms = Map[String, JValue](
      "notificationIid" -> notification.iid,
      "accepted" -> accepted
    )

    val response = post(ServicePath.respondToIntroduction, parms)

    response.success
  }

  def getProfiles(
    connections: List[Connection],
    timeout: Int = 5000
  )(
    callback: AsyncResponse => Unit
  ): InternalId = {

    val parms = Map[String, JValue](
      "connectionIids" -> connections.map(c => c.iid),
      "timeout" -> timeout
    )

    val response = post(ServicePath.getProfiles, parms)

    response.result match {
      case JObject(JField("handle", JString(handle)) :: Nil) =>
        asyncCallbacks += InternalId(handle) -> callback
        InternalId(handle)
      case r => throw new Exception("Get profiles didn't complete successfully")
    }
  }
}
