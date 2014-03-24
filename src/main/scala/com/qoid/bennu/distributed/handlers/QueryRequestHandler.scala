package com.qoid.bennu.distributed.handlers

import com.qoid.bennu.JdbcAssist._
import com.qoid.bennu.JsonAssist._
import com.qoid.bennu.JsonAssist.jsondsl._
import com.qoid.bennu.distributed.DistributedManager
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
    if (queryRequest.historical) {
      processHistorical(connection, queryRequest, injector)
    }

    if (queryRequest.standing) {
      processStanding(connection, queryRequest, injector)
    }
  }

  private def processHistorical(connection: Connection, queryRequest: QueryRequest, injector: ScalaInjector): Unit = {
    val av = injector.instance[AgentView]
    val mapper = findMapperByTypeName(queryRequest.tpe)
    val results = JArray(av.select(queryRequest.query)(mapper).map(_.toJson).toList)
    val responseData = QueryResponse(queryRequest.handle, results)
    val responseMessage = DistributedMessage(DistributedMessageKind.QueryResponse, 1, responseData.toJson)

    injector.instance[DistributedManager].send(connection, responseMessage)
  }

  private def processStanding(connection: Connection, queryRequest: QueryRequest, injector: ScalaInjector): Unit = {
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
}
