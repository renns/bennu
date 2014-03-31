package com.qoid.bennu.testclient.client

import com.qoid.bennu.JdbcAssist
import com.qoid.bennu.JsonAssist._
import com.qoid.bennu.JsonAssist.jsondsl._
import com.qoid.bennu.ServicePath
import com.qoid.bennu.model._
import com.qoid.bennu.model.id._

trait ServiceAssist {
  this: ChannelClient =>

  def upsert[T <: HasInternalId](
    instance: T,
    parentIid: Option[InternalId] = None,
    profileName: Option[String] = None,
    profileImgSrc: Option[String] = None,
    labelIids: Option[List[InternalId]] = None
  ): T = {

    val parms = Map[String, JValue](
      "type" -> instance.mapper.typeName,
      "instance" -> instance.toJson,
      "parentIid" -> parentIid,
      "profileName" -> profileName,
      "profileImgSrc" -> profileImgSrc,
      "labelIids" -> labelIids
    )

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

  def queryLocal[T <: HasInternalId : Manifest](queryStr: String): List[T] = {
    import scala.concurrent._
    import scala.concurrent.duration.Duration

    val p = Promise[List[T]]()

    try {
      query[T](queryStr) { response =>
        try {
          (response.responseType, response.results) match {
            case (QueryResponseType.Query, JArray(r)) =>
              val mapper = JdbcAssist.findMapperByType[T]
              p.success(r.map(mapper.fromJson))
            case (QueryResponseType.Query, JNothing) => p.success(Nil)
            case _ => p.failure(new Exception("Query didn't complete successfully"))
          }
        } catch {
          case e: Exception => p.failure(e)
        }
      }
    } catch {
      case e: Exception => p.failure(e)
    }

    Await.result(p.future, Duration("5 seconds"))
  }

  def query[T <: HasInternalId : Manifest](
    query: String,
    alias: Option[Alias] = None,
    local: Boolean = true,
    connections: List[Connection] = Nil,
    historical: Boolean = true,
    standing: Boolean = false
  )(
    callback: QueryResponse => Unit
  ): InternalId = {

    val typeName = JdbcAssist.findMapperByType[T].typeName

    val parms = Map[String, JValue](
      "type" -> typeName.toLowerCase,
      "q" -> query,
      "aliasIid" -> alias.map(_.iid),
      "local" -> local,
      "connectionIids" -> connections.map(_.iid),
      "historical" -> historical,
      "standing" -> standing
    )

    val response = post(ServicePath.query, parms)

    response.result match {
      case JObject(JField("handle", JString(handle)) :: _) =>
        asyncCallbacks += Handle(handle) -> callback
        InternalId(handle)
      case r => throw new Exception(s"Distributed query result invalid -- $r")
    }
  }

  def deRegisterStandingQuery(handle: Handle): Boolean = {
    asyncCallbacks -= handle
    val parms = Map[String, JValue]("handle" -> handle)
    post(ServicePath.deRegisterStandingQuery, parms).success
  }

  def initiateIntroduction(aConnection: Connection, aMessage: String, bConnection: Connection, bMessage: String): Boolean = {
    val parms = Map[String, JValue](
      "aConnectionIid" -> aConnection.iid,
      "aMessage" -> aMessage,
      "bConnectionIid" -> bConnection.iid,
      "bMessage" -> bMessage
    )

    post(ServicePath.initiateIntroduction, parms).success
  }

  def respondToIntroduction(notification: Notification, accepted: Boolean): Boolean = {
    val parms = Map[String, JValue](
      "notificationIid" -> notification.iid,
      "accepted" -> accepted
    )

    post(ServicePath.respondToIntroduction, parms).success
  }

  def requestVerification(content: Content, connections: List[Connection], message: String): Boolean = {
    val parms = Map[String, JValue](
      "contentIid" -> content.iid,
      "connectionIids" -> connections.map(_.iid),
      "message" -> message
    )

    post(ServicePath.requestVerification, parms).success
  }

  def respondToVerification(notification: Notification, message: String): Boolean = {
    val parms = Map[String, JValue](
      "notificationIid" -> notification.iid,
      "message" -> message
    )

    post(ServicePath.respondToVerification, parms).success
  }

  def verify(connection: Connection, content: Content, message: String): Boolean = {
    val parms = Map[String, JValue](
      "connectionIid" -> connection.iid,
      "contentIid" -> content.iid,
      "contentData" -> content.data,
      "message" -> message
    )

    post(ServicePath.verify, parms).success
  }

  def acceptVerification(notification: Notification): Boolean = {
    val parms = Map[String, JValue]("notificationIid" -> notification.iid)
    post(ServicePath.acceptVerification, parms).success
  }
}
