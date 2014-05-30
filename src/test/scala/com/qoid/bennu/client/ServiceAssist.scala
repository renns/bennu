package com.qoid.bennu.client

import com.qoid.bennu.JdbcAssist
import com.qoid.bennu.JdbcAssist.BennuMapperCompanion
import com.qoid.bennu.JsonAssist._
import com.qoid.bennu.JsonAssist.jsondsl._
import com.qoid.bennu.ServicePath
import com.qoid.bennu.model._
import com.qoid.bennu.model.id._
import com.qoid.bennu.squery.StandingQueryAction
import scala.async.Async._
import scala.concurrent._

trait ServiceAssist {
  this: ChannelClient =>

  def deleteAgent(exportData: Boolean = false): Future[JValue] = {
    async {
      val parms = Map[String, JValue]("exportData" -> exportData)
      val response = await(post(ServicePath.deleteAgent, parms))
      if (response.success) response.result else throw new Exception("Delete Agent didn't complete successfully")
    }
  }

  def upsert[T <: HasInternalId](
    instance: T,
    parentIid: Option[InternalId] = None,
    profileName: Option[String] = None,
    profileImgSrc: Option[String] = None,
    labelIids: List[InternalId] = Nil
  ): Future[T] = {
    async {
      val labelIidsParm = if (labelIids.isEmpty) None else Some(labelIids)

      val parms = Map[String, JValue](
        "type" -> instance.mapper.typeName,
        "instance" -> instance.toJson,
        "parentIid" -> parentIid,
        "profileName" -> profileName,
        "profileImgSrc" -> profileImgSrc,
        "labelIids" -> labelIidsParm
      )

      val response = await(post(ServicePath.upsert, parms))

      response.result match {
        case JNothing => throw new Exception("Upsert didn't complete successfully")
        case r =>
          val mapper = JdbcAssist.findMapperByTypeName(instance.mapper.typeName)
          mapper.fromJson(r).asInstanceOf[T]
      }
    }
  }

  def delete[T <: HasInternalId](iid: InternalId)(implicit mapper: BennuMapperCompanion[T]): Future[T] = {
    async {
      val parms = Map[String, JValue]("type" -> mapper.typeName, "primaryKey" -> iid)

      val response = await(post(ServicePath.delete, parms))

      response.result match {
        case JNothing => throw new Exception(s"Delete didn't complete successfully")
        case r => mapper.fromJson(r)
      }
    }
  }

  def queryLocal[T <: HasInternalId : Manifest](
    queryStr: String,
    aliasIid: Option[InternalId] = None,
    standingResultHandler: Option[(T, StandingQueryAction, Handle) => Unit] = None
  ): Future[List[T]] = {
    val p = Promise[List[T]]()

    try {
      query[T](queryStr, aliasIid, standing = standingResultHandler.nonEmpty) { response =>
        try {
          val mapper = JdbcAssist.findMapperByType[T]

          (response.responseType, response.results, response.action) match {
            case (QueryResponseType.Query, JArray(r), None) => p.success(r.map(mapper.fromJson))
            case (QueryResponseType.Query, JNothing, None) => p.success(Nil)
            case (QueryResponseType.SQuery, JArray(r :: Nil), Some(action)) =>
              standingResultHandler.foreach(_(mapper.fromJson(r), action, response.handle))
            case _ => p.failure(new Exception("Query didn't complete successfully"))
          }
        } catch {
          case e: Exception => p.failure(e)
        }
      }
    } catch {
      case e: Exception => p.failure(e)
    }

    p.future
  }

  def queryRemote[T <: HasInternalId : Manifest](
    queryStr: String,
    connectionChain: List[InternalId],
    aliasIid: Option[InternalId] = None,
    standingResultHandler: Option[(T, StandingQueryAction, Handle) => Unit] = None
  ): Future[List[T]] = {
    val p = Promise[List[T]]()

    try {
      query[T](queryStr, aliasIid, local = false, connectionIids = List(connectionChain), standing = standingResultHandler.nonEmpty) { response =>
        try {
          val mapper = JdbcAssist.findMapperByType[T]

          (response.responseType, response.results, response.action) match {
            case (QueryResponseType.Query, JArray(r), None) => p.success(r.map(mapper.fromJson))
            case (QueryResponseType.Query, JNothing, None) => p.success(Nil)
            case (QueryResponseType.SQuery, JArray(r :: Nil), Some(action)) =>
              standingResultHandler.foreach(_(mapper.fromJson(r), action, response.handle))
            case _ => p.failure(new Exception("Query didn't complete successfully"))
          }
        } catch {
          case e: Exception => p.failure(e)
        }
      }
    } catch {
      case e: Exception => p.failure(e)
    }

    p.future
  }

  def query[T <: HasInternalId : Manifest](
    queryStr: String,
    aliasIid: Option[InternalId] = None,
    local: Boolean = true,
    connectionIids: List[List[InternalId]] = Nil,
    historical: Boolean = true,
    standing: Boolean = false
  )(
    fn: QueryResponse => Unit
  ): Future[Handle] = {
    async {
      val typeName = JdbcAssist.findMapperByType[T].typeName

      val parms = Map[String, JValue](
        "type" -> typeName.toLowerCase,
        "q" -> queryStr,
        "aliasIid" -> aliasIid,
        "local" -> local,
        "connectionIids" -> connectionIids,
        "historical" -> historical,
        "standing" -> standing
      )

      val context = JString(InternalId.random.value)
      asyncCallbacks += context -> fn
      val response = await(post(ServicePath.query, parms, context))

      response.result match {
        case JObject(JField("handle", JString(handle)) :: _) => Handle(handle)
        case r => throw new Exception(s"Distributed query result invalid -- $r")
      }
    }
  }

  def deRegisterStandingQuery(handle: Handle): Future[Boolean] = {
    async {
      val parms = Map[String, JValue]("handle" -> handle)
      await(post(ServicePath.deRegisterStandingQuery, parms)).success
    }
  }

  def initiateIntroduction(aConnection: Connection, aMessage: String, bConnection: Connection, bMessage: String): Future[Boolean] = {
    async {
      val parms = Map[String, JValue](
        "aConnectionIid" -> aConnection.iid,
        "aMessage" -> aMessage,
        "bConnectionIid" -> bConnection.iid,
        "bMessage" -> bMessage
      )

      await(post(ServicePath.initiateIntroduction, parms)).success
    }
  }

  def respondToIntroduction(notification: Notification, accepted: Boolean): Future[Boolean] = {
    async {
      val parms = Map[String, JValue](
        "notificationIid" -> notification.iid,
        "accepted" -> accepted
      )

      await(post(ServicePath.respondToIntroduction, parms)).success
    }
  }

  def requestVerification(content: Content, connections: List[Connection], message: String): Future[Boolean] = {
    async {
      val parms = Map[String, JValue](
        "contentIid" -> content.iid,
        "connectionIids" -> connections.map(_.iid),
        "message" -> message
      )

      await(post(ServicePath.requestVerification, parms)).success
    }
  }

  def respondToVerification(notification: Notification, verificationContent: String): Future[Boolean] = {
    async {
      val parms = Map[String, JValue](
        "notificationIid" -> notification.iid,
        "verificationContent" -> verificationContent
      )

      await(post(ServicePath.respondToVerification, parms)).success
    }
  }

  def verify(connection: Connection, content: Content, verificationContent: String): Future[Boolean] = {
    async {
      val parms = Map[String, JValue](
        "connectionIid" -> connection.iid,
        "contentIid" -> content.iid,
        "contentData" -> content.data,
        "verificationContent" -> verificationContent
      )

      await(post(ServicePath.verify, parms)).success
    }
  }

  def acceptVerification(notification: Notification): Future[Boolean] = {
    async {
      val parms = Map[String, JValue]("notificationIid" -> notification.iid)
      await(post(ServicePath.acceptVerification, parms)).success
    }
  }
}
