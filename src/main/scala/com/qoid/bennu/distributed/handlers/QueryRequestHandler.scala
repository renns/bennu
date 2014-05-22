package com.qoid.bennu.distributed.handlers

import com.qoid.bennu.JdbcAssist._
import com.qoid.bennu.JsonAssist.jsondsl._
import com.qoid.bennu.distributed.DistributedManager
import com.qoid.bennu.distributed.QueryResponseManager
import com.qoid.bennu.distributed.messages._
import com.qoid.bennu.model.Connection
import com.qoid.bennu.model.HasInternalId
import com.qoid.bennu.model.id.Handle
import com.qoid.bennu.security.AgentView
import com.qoid.bennu.squery.StandingQueryAction
import com.qoid.bennu.squery.StandingQueryManager
import m3.predef._

object QueryRequestHandler {
  def handle(connection: Connection, queryRequest: QueryRequest, injector: ScalaInjector): Unit = {
    if (queryRequest.connectionIids.isEmpty) {
      if (queryRequest.historical) {
        processHistorical(connection, queryRequest, injector)
      }

      if (queryRequest.standing) {
        processStanding(connection, queryRequest, injector)
      }
    } else {
      forwardQuery(connection, queryRequest, injector)
    }
  }

  private def processHistorical(connection: Connection, queryRequest: QueryRequest, injector: ScalaInjector): Unit = {
    val results = if (queryRequest.degreesOfVisibility <= connection.allowedDegreesOfVisibility) {
      val av = injector.instance[AgentView]
      val mapper = findMapperByTypeName(queryRequest.tpe)
      av.select(queryRequest.query)(mapper).map(_.toJson).toList
    } else {
      Nil
    }

    val responseData = QueryResponse(queryRequest.handle, results)
    val responseMessage = DistributedMessage(DistributedMessageKind.QueryResponse, 1, responseData.toJson)

    injector.instance[DistributedManager].send(connection, responseMessage)
  }

  private def processStanding(connection: Connection, queryRequest: QueryRequest, injector: ScalaInjector): Unit = {
    if (queryRequest.degreesOfVisibility <= connection.allowedDegreesOfVisibility) {
      val sQueryMgr = injector.instance[StandingQueryManager]

      sQueryMgr.addRemote(
        connection.agentId,
        connection.iid,
        queryRequest.handle,
        queryRequest.tpe,
        queryRequest.query,
        sQueryResponseHandler(_, _, connection, queryRequest.handle, injector)
      )
    }
  }

  private def sQueryResponseHandler(
    instance: HasInternalId,
    action: StandingQueryAction,
    connection: Connection,
    handle: Handle,
    injector: ScalaInjector
  ): Unit = {
    val responseData = QueryResponse(handle, List(instance.toJson), standing = true, action = Some(action))
    val responseMessage = DistributedMessage(DistributedMessageKind.QueryResponse, 1, responseData.toJson)

    injector.instance[DistributedManager].send(connection, responseMessage)
  }

  private def forwardQuery(connection: Connection, queryRequest: QueryRequest, injector: ScalaInjector): Unit = {
    queryRequest.connectionIids match {
      case iid :: iids =>
        val av = injector.instance[AgentView]
        val queryResponseMgr = injector.instance[QueryResponseManager]
        val distributedMgr = injector.instance[DistributedManager]

        val handle = Handle.random

        queryResponseMgr.registerHandle(
          handle,
          distributedResponseHandler(distributedMgr, queryRequest, connection)
        )

        av.fetchOpt[Connection](iid).foreach { toConnection =>
          val request = queryRequest.copy(handle = handle, degreesOfVisibility = queryRequest.degreesOfVisibility + 1, connectionIids = iids)
          distributedMgr.send(toConnection, DistributedMessage(DistributedMessageKind.QueryRequest, 1, request.toJson))
        }
      case _ =>
    }
  }

  private def distributedResponseHandler(
    distributedMgr: DistributedManager,
    queryRequest: QueryRequest,
    requestConnection: Connection
  )(
    connection: Connection,
    queryResponse: QueryResponse
  ): Unit = {
    val responseData = queryResponse.copy(handle = queryRequest.handle)
    val responseMessage = DistributedMessage(DistributedMessageKind.QueryResponse, 1, responseData.toJson)
    distributedMgr.send(requestConnection, responseMessage)
  }
}
