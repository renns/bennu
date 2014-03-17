package com.qoid.bennu.testclient.client

import com.qoid.bennu.JdbcAssist
import com.qoid.bennu.JsonAssist._
import com.qoid.bennu.JsonAssist.jsondsl._
import com.qoid.bennu.ServicePath
import com.qoid.bennu.model._

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

  def queryLocal[T <: HasInternalId : Manifest](queryStr: String): List[T] = {
    import scala.concurrent._
    import scala.concurrent.duration.Duration

    val p = Promise[List[T]]()

    try {
      query[T](queryStr) { response =>
        try {
          (response.responseType, response.results) match {
            case (QueryResponseType.Query, JArray(r)) =>
              val typeName = manifest[T].runtimeClass.getSimpleName
              val mapper = JdbcAssist.findMapperByTypeName(typeName)
              p.success(r.map(mapper.fromJson(_).asInstanceOf[T]))
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
    standing: Boolean = false,
    context: JValue = JNothing
  )(
    callback: QueryResponse => Unit
  ): InternalId = {

    val typeName = manifest[T].runtimeClass.getSimpleName

    val parms = Map[String, JValue](
      "type" -> typeName.toLowerCase,
      "q" -> query,
      "aliasIid" -> alias.map(_.iid),
      "local" -> local,
      "connectionIids" -> connections.map(c => c.iid),
      "historical" -> historical,
      "standing" -> standing,
      "context" -> context
    )

    val response = post(ServicePath.query, parms, Some(context))

    response.result match {
      case JObject(JField("handle", JString(handle)) :: Nil) =>
        asyncCallbacks += Handle(handle) -> callback
        InternalId(handle)
      case r => throw new Exception(s"Distributed query result invalid -- $r")
    }
  }

  def deRegisterStandingQuery(handle: Handle): Boolean = {
    asyncCallbacks -= handle
    val parms = Map[String, JValue]("handle" -> handle)
    val response = post(ServicePath.deRegisterStandingQuery, parms)
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
}
