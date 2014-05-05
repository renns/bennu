package com.qoid.bennu.testclient.client

import com.qoid.bennu.JdbcAssist
import com.qoid.bennu.JsonAssist._
import com.qoid.bennu.JsonAssist.jsondsl._
import com.qoid.bennu.ServicePath
import com.qoid.bennu.model._
import com.qoid.bennu.model.id._
import com.qoid.bennu.JdbcAssist.BennuMapperCompanion

trait ServiceAssist {
  this: ChannelClient =>

  def deleteAgent(exportData: Boolean): JValue = {
    val parms = Map[String, JValue]("exportData" -> exportData)
    val response = post(ServicePath.deleteAgent, parms)
    if (response.success) response.result else throw new Exception("Delete Agent didn't complete successfully")
  }

  def upsert[T <: HasInternalId](
    instance: T,
    parentIid: Option[InternalId] = None,
    profileName: Option[String] = None,
    profileImgSrc: Option[String] = None,
    labelIids: List[InternalId] = Nil
  ): T = {

    val labelIidsParm = if (labelIids.isEmpty) None else Some(labelIids)

    val parms = Map[String, JValue](
      "type" -> instance.mapper.typeName,
      "instance" -> instance.toJson,
      "parentIid" -> parentIid,
      "profileName" -> profileName,
      "profileImgSrc" -> profileImgSrc,
      "labelIids" -> labelIidsParm
    )

    val response = post(ServicePath.upsert, parms)

    response.result match {
      case JNothing => throw new Exception("Upsert didn't complete successfully")
      case r =>
        val mapper = JdbcAssist.findMapperByTypeName(instance.mapper.typeName)
        mapper.fromJson(r).asInstanceOf[T]
    }
  }

  def delete[T <: HasInternalId](iid: InternalId)(implicit mapper: BennuMapperCompanion[T]): T = {
    val parms = Map[String, JValue]("type" -> mapper.typeName, "primaryKey" -> iid)

    val response = post(ServicePath.delete, parms)

    response.result match {
      case JNothing => throw new Exception(s"Delete didn't complete successfully")
      case r => mapper.fromJson(r)
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
    aliasIid: Option[InternalId] = None,
    local: Boolean = true,
    connectionIids: List[InternalId] = Nil,
    historical: Boolean = true,
    standing: Boolean = false
  )(
    fn: QueryResponse => Unit
  ): Handle = {

    val typeName = JdbcAssist.findMapperByType[T].typeName

    val parms = Map[String, JValue](
      "type" -> typeName.toLowerCase,
      "q" -> query,
      "aliasIid" -> aliasIid,
      "local" -> local,
      "connectionIids" -> connectionIids,
      "historical" -> historical,
      "standing" -> standing
    )

    val response = post(ServicePath.query, parms)

    response.result match {
      case JObject(JField("handle", JString(handle)) :: _) =>
        asyncCallbacks += Handle(handle) -> fn
        Handle(handle)
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

  def respondToVerification(notification: Notification, verificationContent: String): Boolean = {
    val parms = Map[String, JValue](
      "notificationIid" -> notification.iid,
      "verificationContent" -> verificationContent
    )

    post(ServicePath.respondToVerification, parms).success
  }

  def verify(connection: Connection, content: Content, verificationContent: String): Boolean = {
    val parms = Map[String, JValue](
      "connectionIid" -> connection.iid,
      "contentIid" -> content.iid,
      "contentData" -> content.data,
      "verificationContent" -> verificationContent
    )

    post(ServicePath.verify, parms).success
  }

  def acceptVerification(notification: Notification): Boolean = {
    val parms = Map[String, JValue]("notificationIid" -> notification.iid)
    post(ServicePath.acceptVerification, parms).success
  }
}
