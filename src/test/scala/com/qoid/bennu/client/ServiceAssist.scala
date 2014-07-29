package com.qoid.bennu.client

import com.qoid.bennu.ErrorCode
import com.qoid.bennu.ServicePath
import com.qoid.bennu.distributed.DistributedResult
import com.qoid.bennu.distributed.messages.CreateLabelResponse
import com.qoid.bennu.distributed.messages.DistributedMessageKind
import com.qoid.bennu.distributed.messages.QueryResponse
import com.qoid.bennu.mapper.MapperAssist
import com.qoid.bennu.model.Label
import com.qoid.bennu.model.id.InternalId
import com.qoid.bennu.JsonAssist._
import com.qoid.bennu.JsonAssist.jsondsl._
import com.qoid.bennu.query.StandingQueryAction
import m3.servlet.beans.MultiRequestHandler.MethodInvocationResult

import scala.concurrent.Future

trait ServiceAssist {
  this: ChannelClient =>

  def logout(): Future[Unit] = {
    post(ServicePath.logout, Map.empty[String, JValue]).map(_ => ())
  }

  def query[T : Manifest](query: String, route: List[InternalId] = List(connectionIid)): Future[List[T]] = {
    val typeName = MapperAssist.findMapperByType[T].typeName

    val parms = Map[String, JValue](
      "route" -> route,
      "type" -> typeName,
      "query" -> query,
      "historical" -> true,
      "standing" -> false
    )

    transformResult[QueryResponse](
      submit(ServicePath.query, parms),
      DistributedMessageKind.QueryResponse
    ).map(_.results.map(serializer.fromJson[T]))
  }

  def queryStanding[T : Manifest](
    query: String,
    route: List[InternalId] = List(connectionIid)
  )(
    fn: (T, StandingQueryAction, JValue) => Unit
  ): Future[List[T]] = {

    val typeName = MapperAssist.findMapperByType[T].typeName

    val parms = Map[String, JValue](
      "route" -> route,
      "type" -> typeName,
      "query" -> query,
      "historical" -> true,
      "standing" -> true
    )

    //TODO: specify context
    //TODO: add context / fn to callbacks map
    //TODO: move callbacks map to this class

    transformResult[QueryResponse](
      submit(ServicePath.query, parms),
      DistributedMessageKind.QueryResponse
    ).map(_.results.map(serializer.fromJson[T]))
  }

  def createLabel(parentLabelIid: InternalId, name: String, route: List[InternalId] = List(connectionIid)): Future[Label] = {
    val parms = Map[String, JValue](
      "route" -> route,
      "parentLabelIid" -> parentLabelIid,
      "name" -> name,
      "data" -> ("color" -> "#7F7F7F")
    )

    transformResult[CreateLabelResponse](
      submit(ServicePath.createLabel, parms),
      DistributedMessageKind.CreateLabelResponse
    ).map(_.label)
  }

  private def transformResult[T : Manifest](f: Future[MethodInvocationResult], kind: DistributedMessageKind): Future[T] = {
    f.map { result1 =>
      (result1.success, result1.error) match {
        case (true, _) =>
          val result2 = DistributedResult.fromJson(result1.result)

          if (result2.kind == kind) {
            serializer.fromJson[T](result2.result)
          } else {
            throw new Exception(ErrorCode.unsupportedResponseMessage)
          }
        case (false, Some(error)) => throw new Exception(error.message)
        case (false, None) => throw new Exception(ErrorCode.unexpectedError)
      }
    }
  }
}

//  def deleteAgent(exportData: Boolean = false): Future[JValue] = {
//    async {
//      val parms = Map[String, JValue]("exportData" -> exportData)
//      val response = await(post(ServicePath.deleteAgent, parms))
//      if (response.success) response.result else throw new Exception("Delete Agent didn't complete successfully")
//    }
//  }
//
//  def upsert[T <: HasInternalId](
//    instance: T,
//    parentIid: Option[InternalId] = None,
//    profileName: Option[String] = None,
//    profileImgSrc: Option[String] = None,
//    labelIids: List[InternalId] = Nil
//  ): Future[T] = {
//    async {
//      val labelIidsParm = if (labelIids.isEmpty) None else Some(labelIids)
//
//      val parms = Map[String, JValue](
//        "type" -> instance.mapper.typeName,
//        "instance" -> instance.toJson,
//        "parentIid" -> parentIid,
//        "profileName" -> profileName,
//        "profileImgSrc" -> profileImgSrc,
//        "labelIids" -> labelIidsParm
//      )
//
//      val response = await(post(ServicePath.upsert, parms))
//
//      response.result match {
//        case JNothing => throw new Exception("Upsert didn't complete successfully")
//        case r =>
//          val mapper = JdbcAssist.findMapperByTypeName(instance.mapper.typeName)
//          mapper.fromJson(r).asInstanceOf[T]
//      }
//    }
//  }
//
//  def delete[T <: HasInternalId](iid: InternalId)(implicit mapper: BennuMapperCompanion[T]): Future[T] = {
//    async {
//      val parms = Map[String, JValue]("type" -> mapper.typeName, "primaryKey" -> iid)
//
//      val response = await(post(ServicePath.delete, parms))
//
//      response.result match {
//        case JNothing => throw new Exception(s"Delete didn't complete successfully")
//        case r => mapper.fromJson(r)
//      }
//    }
//  }
//
//  def queryLocal[T <: HasInternalId : Manifest](
//    queryStr: String,
//    aliasIid: Option[InternalId] = None,
//    standingResultHandler: Option[(T, StandingQueryAction, Handle) => Unit] = None
//  ): Future[List[T]] = {
//    val p = Promise[List[T]]()
//
//    try {
//      query[T](queryStr, aliasIid, standing = standingResultHandler.nonEmpty) { response =>
//        try {
//          val mapper = JdbcAssist.findMapperByType[T]
//
//          (response.responseType, response.results, response.action) match {
//            case (QueryResponseType.Query, JArray(r), None) => p.success(r.map(mapper.fromJson))
//            case (QueryResponseType.Query, JNothing, None) => p.success(Nil)
//            case (QueryResponseType.SQuery, JArray(r :: Nil), Some(action)) =>
//              standingResultHandler.foreach(_(mapper.fromJson(r), action, response.handle))
//            case _ => p.failure(new Exception("Query didn't complete successfully"))
//          }
//        } catch {
//          case e: Exception => p.failure(e)
//        }
//      }
//    } catch {
//      case e: Exception => p.failure(e)
//    }
//
//    p.future
//  }
//
//  def queryRemote[T <: HasInternalId : Manifest](
//    queryStr: String,
//    connectionChain: List[InternalId],
//    aliasIid: Option[InternalId] = None,
//    standingResultHandler: Option[(T, StandingQueryAction, Handle) => Unit] = None
//  ): Future[List[T]] = {
//    val p = Promise[List[T]]()
//
//    try {
//      query[T](queryStr, aliasIid, local = false, connectionIids = List(connectionChain), standing = standingResultHandler.nonEmpty) { response =>
//        try {
//          val mapper = JdbcAssist.findMapperByType[T]
//
//          (response.responseType, response.results, response.action) match {
//            case (QueryResponseType.Query, JArray(r), None) => p.success(r.map(mapper.fromJson))
//            case (QueryResponseType.Query, JNothing, None) => p.success(Nil)
//            case (QueryResponseType.SQuery, JArray(r :: Nil), Some(action)) =>
//              standingResultHandler.foreach(_(mapper.fromJson(r), action, response.handle))
//            case _ => p.failure(new Exception("Query didn't complete successfully"))
//          }
//        } catch {
//          case e: Exception => p.failure(e)
//        }
//      }
//    } catch {
//      case e: Exception => p.failure(e)
//    }
//
//    p.future
//  }

//  def deRegisterStandingQuery(handle: Handle): Future[Boolean] = {
//    async {
//      val parms = Map[String, JValue]("handle" -> handle)
//      await(post(ServicePath.deRegisterStandingQuery, parms)).success
//    }
//  }
//
//  def initiateIntroduction(aConnection: Connection, aMessage: String, bConnection: Connection, bMessage: String): Future[Boolean] = {
//    async {
//      val parms = Map[String, JValue](
//        "aConnectionIid" -> aConnection.iid,
//        "aMessage" -> aMessage,
//        "bConnectionIid" -> bConnection.iid,
//        "bMessage" -> bMessage
//      )
//
//      await(post(ServicePath.initiateIntroduction, parms)).success
//    }
//  }
//
//  def respondToIntroduction(notification: Notification, accepted: Boolean): Future[Boolean] = {
//    async {
//      val parms = Map[String, JValue](
//        "notificationIid" -> notification.iid,
//        "accepted" -> accepted
//      )
//
//      await(post(ServicePath.respondToIntroduction, parms)).success
//    }
//  }
//
//  def requestVerification(content: Content, connections: List[Connection], message: String): Future[Boolean] = {
//    async {
//      val parms = Map[String, JValue](
//        "contentIid" -> content.iid,
//        "connectionIids" -> connections.map(_.iid),
//        "message" -> message
//      )
//
//      await(post(ServicePath.requestVerification, parms)).success
//    }
//  }
//
//  def respondToVerification(notification: Notification, verificationContent: String): Future[Boolean] = {
//    async {
//      val parms = Map[String, JValue](
//        "notificationIid" -> notification.iid,
//        "verificationContent" -> verificationContent
//      )
//
//      await(post(ServicePath.respondToVerification, parms)).success
//    }
//  }
//
//  def verify(connection: Connection, content: Content, verificationContent: String): Future[Boolean] = {
//    async {
//      val parms = Map[String, JValue](
//        "connectionIid" -> connection.iid,
//        "contentIid" -> content.iid,
//        "contentData" -> content.data,
//        "verificationContent" -> verificationContent
//      )
//
//      await(post(ServicePath.verify, parms)).success
//    }
//  }
//
//  def acceptVerification(notification: Notification): Future[Boolean] = {
//    async {
//      val parms = Map[String, JValue]("notificationIid" -> notification.iid)
//      await(post(ServicePath.acceptVerification, parms)).success
//    }
//  }
//}
