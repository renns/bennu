package com.qoid.bennu.client

import com.qoid.bennu.JsonAssist._
import com.qoid.bennu.JsonAssist.jsondsl._
import com.qoid.bennu.ServicePath
import com.qoid.bennu.mapper.MapperAssist
import com.qoid.bennu.model._
import com.qoid.bennu.model.id.InternalId
import com.qoid.bennu.model.service.QueryResult
import com.qoid.bennu.query.StandingQueryAction
import scala.async.Async._
import scala.concurrent.Future
import scala.concurrent.Promise

trait ServiceAssist {
  this: ChannelClient =>

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
}
