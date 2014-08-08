package com.qoid.bennu.client

import com.qoid.bennu.JsonAssist._
import com.qoid.bennu.JsonAssist.jsondsl._
import com.qoid.bennu.mapper.MapperAssist
import com.qoid.bennu.model._
import com.qoid.bennu.model.id.InternalId
import com.qoid.bennu.model.service.QueryResult
import com.qoid.bennu.query.StandingQueryAction
import com.qoid.bennu.webservices.ServicePath
import scala.async.Async._
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scala.concurrent.Promise

trait ServiceAssist {
  this: ChannelClient =>

  def spawnSession[T](
    aliasIid: InternalId
  )(
    body: ChannelClient => Future[T]
  )(
    implicit
    config: HttpClientConfig,
    ec: ExecutionContext
  ): Future[T] = {
    async {
      val parms = Map[String, JValue](
        "aliasIid" -> aliasIid
      )

      val result = await(post(ServicePath.spawnSession, parms))
      val session = serializer.fromJson[Session](result)
      val client = new HttpChannelClient(session.channelId, session.connectionIid)
      val t = await(body(client))

      client.close()
      await(client.logout())

      t
    }
  }

  def logout(): Future[Unit] = {
    post(ServicePath.logout, Map.empty[String, JValue]).map(_ => ())
  }

  def query[T : Manifest](query: String, route: List[InternalId] = List(connectionIid)): Future[List[T]] = {
    async {
      val typeName = MapperAssist.findMapperByType[T].typeName

      val parms = Map[String, JValue](
        "route" -> route,
        "type" -> typeName,
        "query" -> query,
        "historical" -> true,
        "standing" -> false
      )

      val result = await(submitSingleResponse(ServicePath.query, parms))
      val queryResult = serializer.fromJson[QueryResult](result)
      queryResult.results.map(serializer.fromJson[T])
    }
  }

  def queryStanding[T : Manifest](
    query: String,
    route: List[InternalId] = List(connectionIid)
  )(
    fn: (T, StandingQueryAction, JValue) => Unit
  ): Future[List[T]] = {

    val promise = Promise[List[T]]()

    val typeName = MapperAssist.findMapperByType[T].typeName

    val parms = Map[String, JValue](
      "route" -> route,
      "type" -> typeName,
      "query" -> query,
      "historical" -> true,
      "standing" -> true
    )

    submit(ServicePath.query, parms) { result =>
      (result.success, result.error) match {
        case (true, _) =>
          val queryResult = serializer.fromJson[QueryResult](result.result)

          if (queryResult.standing) {
            val t = queryResult.results.map(serializer.fromJson[T]).head
            fn(t, queryResult.action.get, result.context)
          } else {
            promise.success(queryResult.results.map(serializer.fromJson[T]))
          }
        case (false, Some(error)) => promise.failure(new Exception(error.message))
        case (false, None) => promise.failure(new Exception("Failed with no error"))
      }
    }

    promise.future
  }

  def createAlias(
    name: String,
    profileName: String,
    profileImage: Option[String] = None,
    data: Option[JValue] = None,
    route: List[InternalId] = List(connectionIid)
  ): Future[Alias] = {

    async {
      val parms = Map[String, JValue](
        "route" -> route,
        "name" -> name,
        "profileName" -> profileName,
        "profileImage" -> profileImage,
        "data" -> data
      )

      val result = await(submitSingleResponse(ServicePath.createAlias, parms))
      Alias.fromJson(result)
    }
  }

  def updateAlias(
    aliasIid: InternalId,
    data: JValue,
    route: List[InternalId] = List(connectionIid)
  ): Future[Alias] = {

    async {
      val parms = Map[String, JValue](
        "route" -> route,
        "aliasIid" -> aliasIid,
        "data" -> data
      )

      val result = await(submitSingleResponse(ServicePath.updateAlias, parms))
      Alias.fromJson(result)
    }
  }

  def deleteAlias(
    aliasIid: InternalId,
    route: List[InternalId] = List(connectionIid)
  ): Future[InternalId] = {

    async {
      val parms = Map[String, JValue](
        "route" -> route,
        "aliasIid" -> aliasIid
      )

      val result = await(submitSingleResponse(ServicePath.deleteAlias, parms))
      serializer.fromJson[AliasIid](result).aliasIid
    }
  }

  def createAliasLogin(
    aliasIid: InternalId,
    password: String,
    route: List[InternalId] = List(connectionIid)
  ): Future[Login] = {

    async {
      val parms = Map[String, JValue](
        "route" -> route,
        "aliasIid" -> aliasIid,
        "password" -> password
      )

      val result = await(submitSingleResponse(ServicePath.createAliasLogin, parms))
      Login.fromJson(result)
    }
  }

  def updateAliasLogin(
    aliasIid: InternalId,
    password: String,
    route: List[InternalId] = List(connectionIid)
  ): Future[Login] = {

    async {
      val parms = Map[String, JValue](
        "route" -> route,
        "aliasIid" -> aliasIid,
        "password" -> password
      )

      val result = await(submitSingleResponse(ServicePath.updateAliasLogin, parms))
      Login.fromJson(result)
    }
  }

  def deleteAliasLogin(
    aliasIid: InternalId,
    route: List[InternalId] = List(connectionIid)
  ): Future[InternalId] = {

    async {
      val parms = Map[String, JValue](
        "route" -> route,
        "aliasIid" -> aliasIid
      )

      val result = await(submitSingleResponse(ServicePath.deleteAliasLogin, parms))
      serializer.fromJson[AliasIid](result).aliasIid
    }
  }

  def updateAliasProfile(
    aliasIid: InternalId,
    profileName: Option[String] = None,
    profileImage: Option[String] = None,
    route: List[InternalId] = List(connectionIid)
  ): Future[Profile] = {

    async {
      val parms = Map[String, JValue](
        "route" -> route,
        "aliasIid" -> aliasIid,
        "profileName" -> profileName,
        "profileImage" -> profileImage
      )

      val result = await(submitSingleResponse(ServicePath.updateAliasProfile, parms))
      Profile.fromJson(result)
    }
  }

  def deleteConnection(
    connectionIid: InternalId,
    route: List[InternalId] = List(connectionIid)
  ): Future[InternalId] = {

    async {
      val parms = Map[String, JValue](
        "route" -> route,
        "connectionIid" -> connectionIid
      )

      val result = await(submitSingleResponse(ServicePath.deleteConnection, parms))
      serializer.fromJson[ConnectionIid](result).connectionIid
    }
  }

  def createContent(
    contentType: String,
    data: JValue,
    labelIids: List[InternalId],
    route: List[InternalId] = List(connectionIid)
  ): Future[Content] = {

    async {
      val parms = Map[String, JValue](
        "route" -> route,
        "contentType" -> contentType,
        "data" -> data,
        "labelIids" -> labelIids
      )

      val result = await(submitSingleResponse(ServicePath.createContent, parms))
      Content.fromJson(result)
    }
  }

  def updateContent(
    contentIid: InternalId,
    data: JValue,
    route: List[InternalId] = List(connectionIid)
  ): Future[Content] = {

    async {
      val parms = Map[String, JValue](
        "route" -> route,
        "contentIid" -> contentIid,
        "data" -> data
      )

      val result = await(submitSingleResponse(ServicePath.updateContent, parms))
      Content.fromJson(result)
    }
  }

  def addContentLabel(
    contentIid: InternalId,
    labelIid: InternalId,
    route: List[InternalId] = List(connectionIid)
  ): Future[ContentLabel] = {

    async {
      val parms = Map[String, JValue](
        "route" -> route,
        "contentIid" -> contentIid,
        "labelIid" -> labelIid
      )

      val result = await(submitSingleResponse(ServicePath.addContentLabel, parms))
      serializer.fromJson[ContentLabel](result)
    }
  }

  def removeContentLabel(
    contentIid: InternalId,
    labelIid: InternalId,
    route: List[InternalId] = List(connectionIid)
  ): Future[ContentLabel] = {

    async {
      val parms = Map[String, JValue](
        "route" -> route,
        "contentIid" -> contentIid,
        "labelIid" -> labelIid
      )

      val result = await(submitSingleResponse(ServicePath.removeContentLabel, parms))
      serializer.fromJson[ContentLabel](result)
    }
  }

  def createLabel(
    parentLabelIid: InternalId,
    name: String,
    route: List[InternalId] = List(connectionIid)
  ): Future[Label] = {

    async {
      val parms = Map[String, JValue](
        "route" -> route,
        "parentLabelIid" -> parentLabelIid,
        "name" -> name,
        "data" -> ("color" -> "#7F7F7F")
      )

      val result = await(submitSingleResponse(ServicePath.createLabel, parms))
      Label.fromJson(result)
    }
  }

  def updateLabel(
    labelIid: InternalId,
    name: String,
    route: List[InternalId] = List(connectionIid)
  ): Future[Label] = {

    async {
      val parms = Map[String, JValue](
        "route" -> route,
        "labelIid" -> labelIid,
        "name" -> name
      )

      val result = await(submitSingleResponse(ServicePath.updateLabel, parms))
      Label.fromJson(result)
    }
  }

  def moveLabel(
    labelIid: InternalId,
    oldParentLabelIid: InternalId,
    newParentLabelIid: InternalId,
    route: List[InternalId] = List(connectionIid)
  ): Future[InternalId] = {

    async {
      val parms = Map[String, JValue](
        "route" -> route,
        "labelIid" -> labelIid,
        "oldParentLabelIid" -> oldParentLabelIid,
        "newParentLabelIid" -> newParentLabelIid
      )

      val result = await(submitSingleResponse(ServicePath.moveLabel, parms))
      serializer.fromJson[LabelIid](result).labelIid
    }
  }

  def copyLabel(
    labelIid: InternalId,
    newParentLabelIid: InternalId,
    route: List[InternalId] = List(connectionIid)
  ): Future[InternalId] = {

    async {
      val parms = Map[String, JValue](
        "route" -> route,
        "labelIid" -> labelIid,
        "newParentLabelIid" -> newParentLabelIid
      )

      val result = await(submitSingleResponse(ServicePath.copyLabel, parms))
      serializer.fromJson[LabelIid](result).labelIid
    }
  }

  def removeLabel(
    labelIid: InternalId,
    parentLabelIid: InternalId,
    route: List[InternalId] = List(connectionIid)
  ): Future[InternalId] = {

    async {
      val parms = Map[String, JValue](
        "route" -> route,
        "labelIid" -> labelIid,
        "parentLabelIid" -> parentLabelIid
      )

      val result = await(submitSingleResponse(ServicePath.removeLabel, parms))
      serializer.fromJson[LabelIid](result).labelIid
    }
  }

  def grantLabelAccess(
    labelIid: InternalId,
    connectionIid: InternalId,
    maxDoV: Int,
    route: List[InternalId] = List(connectionIid)
  ): Future[InternalId] = {

    async {
      val parms = Map[String, JValue](
        "route" -> route,
        "labelIid" -> labelIid,
        "connectionIid" -> connectionIid,
        "maxDoV" -> JInt(maxDoV)
      )

      val result = await(submitSingleResponse(ServicePath.grantLabelAccess, parms))
      serializer.fromJson[LabelIid](result).labelIid
    }
  }

  def revokeLabelAccess(
    labelIid: InternalId,
    connectionIid: InternalId,
    route: List[InternalId] = List(connectionIid)
  ): Future[InternalId] = {

    async {
      val parms = Map[String, JValue](
        "route" -> route,
        "labelIid" -> labelIid,
        "connectionIid" -> connectionIid
      )

      val result = await(submitSingleResponse(ServicePath.revokeLabelAccess, parms))
      serializer.fromJson[LabelIid](result).labelIid
    }
  }

  def updateLabelAccess(
    labelIid: InternalId,
    connectionIid: InternalId,
    maxDoV: Int,
    route: List[InternalId] = List(connectionIid)
  ): Future[InternalId] = {

    async {
      val parms = Map[String, JValue](
        "route" -> route,
        "labelIid" -> labelIid,
        "connectionIid" -> connectionIid,
        "maxDoV" -> JInt(maxDoV)
      )

      val result = await(submitSingleResponse(ServicePath.updateLabelAccess, parms))
      serializer.fromJson[LabelIid](result).labelIid
    }
  }

  def consumeNotification(
    notificationIid: InternalId,
    route: List[InternalId] = List(connectionIid)
  ): Future[InternalId] = {

    async {
      val parms = Map[String, JValue](
        "route" -> route,
        "notificationIid" -> notificationIid
      )

      val result = await(submitSingleResponse(ServicePath.consumeNotification, parms))
      serializer.fromJson[NotificationIid](result).notificationIid
    }
  }
}
