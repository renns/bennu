package com.qoid.bennu.client

import com.qoid.bennu.JsonAssist._
import com.qoid.bennu.JsonAssist.jsondsl._
import com.qoid.bennu.ServicePath
import com.qoid.bennu.mapper.MapperAssist
import com.qoid.bennu.model._
import com.qoid.bennu.model.id.InternalId
import com.qoid.bennu.model.service.QueryResult
import com.qoid.bennu.query.StandingQueryAction

import scala.concurrent.Future
import scala.concurrent.Promise

trait ServiceAssist {
  this: ChannelClient =>

  def logout(): Future[Unit] = {
    post(ServicePath.logout, Map.empty[String, JValue]).map(_ => ())
  }

  def query[T : Manifest](query: String, route: List[InternalId] = List(connectionIid)): Future[List[T]] = {
    val promise = Promise[List[T]]()

    val typeName = MapperAssist.findMapperByType[T].typeName

    val parms = Map[String, JValue](
      "route" -> route,
      "type" -> typeName,
      "query" -> query,
      "historical" -> true,
      "standing" -> false
    )

    submit(ServicePath.query, parms) { result =>
      cancelSubmit(result.context)

      (result.success, result.error) match {
        case (true, _) =>
          val queryResult = serializer.fromJson[QueryResult](result.result)

          if (!queryResult.standing) {
            promise.success(queryResult.results.map(serializer.fromJson[T]))
          }
        case (false, Some(error)) => promise.failure(new Exception(error.message))
        case (false, None) => promise.failure(new Exception("Failed with no error"))
      }
    }

    promise.future
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

    //TODO: move callbacks map to this class

    submit(ServicePath.query, parms) { result =>
      cancelSubmit(result.context)

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

  def createContent(contentType: String, data: JValue, labelIids: List[InternalId], route: List[InternalId] = List(connectionIid)): Future[Content] = {
    val promise = Promise[Content]()

    val parms = Map[String, JValue](
      "route" -> route,
      "contentType" -> contentType,
      "data" -> data,
      "labelIids" -> labelIids
    )

    submit(ServicePath.createContent, parms) { result =>
      cancelSubmit(result.context)

      (result.success, result.error) match {
        case (true, _) => promise.success(Content.fromJson(result.result))
        case (false, Some(error)) => promise.failure(new Exception(error.message))
        case (false, None) => promise.failure(new Exception("Failed with no error"))
      }
    }

    promise.future
  }

  def createLabel(parentLabelIid: InternalId, name: String, route: List[InternalId] = List(connectionIid)): Future[Label] = {
    val promise = Promise[Label]()

    val parms = Map[String, JValue](
      "route" -> route,
      "parentLabelIid" -> parentLabelIid,
      "name" -> name,
      "data" -> ("color" -> "#7F7F7F")
    )

    submit(ServicePath.createLabel, parms) { result =>
      cancelSubmit(result.context)

      (result.success, result.error) match {
        case (true, _) => promise.success(Label.fromJson(result.result))
        case (false, Some(error)) => promise.failure(new Exception(error.message))
        case (false, None) => promise.failure(new Exception("Failed with no error"))
      }
    }

    promise.future
  }
}
