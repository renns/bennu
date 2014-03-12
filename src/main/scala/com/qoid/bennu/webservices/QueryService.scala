package com.qoid.bennu.webservices

import com.google.inject.Inject
import com.qoid.bennu.FromJsonCapable
import com.qoid.bennu.JdbcAssist._
import com.qoid.bennu.JsonAssist._
import com.qoid.bennu.JsonAssist.jsondsl._
import com.qoid.bennu.SecurityContext
import com.qoid.bennu.SecurityContext.AgentCapableSecurityContext
import com.qoid.bennu.ToJsonCapable
import com.qoid.bennu.distributed.DistributedManager
import com.qoid.bennu.distributed.handlers.QueryResponseHandler
import com.qoid.bennu.distributed.messages._
import com.qoid.bennu.model._
import com.qoid.bennu.squery.StandingQueryAction
import com.qoid.bennu.squery.StandingQueryManager
import com.qoid.bennu.squery.ast.Query
import java.sql.{ Connection => JdbcConn }
import m3.Txn
import m3.json.Json
import m3.predef._
import m3.servlet.beans.Parm
import m3.servlet.longpoll.ChannelId
import net.codingwell.scalaguice.InjectorExtensions.ScalaInjector
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.future

case class QueryService @Inject()(
  injector: ScalaInjector,
  distributedMgr: DistributedManager,
  sQueryMgr: StandingQueryManager,
  securityContext: AgentCapableSecurityContext,
  channelId: ChannelId,
  @Parm("type") _type: String,
  @Parm("q") queryStr: String,
  @Parm ("aliasIid") aliasIidParm: Option[InternalId] = None,
  @Parm connectionIids: List[InternalId] = Nil,
  @Parm historical: Boolean = true,
  @Parm standing: Boolean = false,
  @Parm context: JValue = JNothing
) extends Logging {

  implicit def jdbcConn = injector.instance[JdbcConn]
  val handle = Handle.random

  def service: JValue = {

    val aliasIid = aliasIidParm match {
      case Some(iid) => iid
      case _ => securityContext.aliasIid
    }

    if (aliasIidParm.nonEmpty || connectionIids.isEmpty) submitLocalQuery(aliasIid)
    if (connectionIids.nonEmpty) submitRemoteQuery()

    "handle" -> handle
  }

  def submitLocalQuery(aliasIid: InternalId): Unit = {
    if (historical) {
      future {
        Txn {
          val sc = SecurityContext.AliasSecurityContext(aliasIid)
          Txn.setViaTypename[SecurityContext](sc)
          val agentView = sc.createView
          implicit val mapper = findMapperByTypeName(_type)
          val results = agentView.select(queryStr).toList
          val responseData = QueryService.ResponseData(Some(sc.aliasIid), None, _type, None, results.map(_.toJson))
          AsyncResponse(AsyncResponseType.Query, handle, true, responseData.toJson, context)
            .send(channelId)
        }
      }
    }

    if (standing) {
      sQueryMgr.add(
        securityContext.agentId,
        aliasIid,
        handle,
        _type,
        Query.parse(queryStr),
        QueryService.sQueryResponseHandler(_, _, aliasIid, handle, _type, context, channelId)
      )
    }
  }

  def submitRemoteQuery(): Unit = {
    QueryResponseHandler.registerHandle(
      handle,
      QueryService.distributedResponseHandler(_, _, handle, _type, context, channelId)
    )

    val request = QueryRequest(_type, queryStr, historical, standing, handle, context)

    connectionIids.foreach { connectionIid =>
    // TODO: verify each connection that it exists under the right alias context
      val connection = Connection.fetch(connectionIid)
      distributedMgr.send(connection, DistributedMessage(DistributedMessageKind.QueryRequest, 1, request.toJson))
    }
  }
}

object QueryService extends Logging {
  def sQueryResponseHandler(
    instance: HasInternalId,
    action: StandingQueryAction,
    aliasIid: InternalId,
    handle: Handle,
    tpe: String,
    context: JValue,
    channelId: ChannelId
  ): Unit = {
    logger.warn(s"$instance -- $action -- $aliasIid -- $handle -- $tpe -- $context -- $channelId")
    val responseData = QueryService.ResponseData(Some(aliasIid), None, tpe, Some(action), List(instance.toJson))
    logger.warn(responseData)
    val ar = AsyncResponse(AsyncResponseType.SQuery, handle, true, responseData.toJson, context)
    logger.warn(ar)
    ar.send(channelId)
  }

  def distributedResponseHandler(
    connection: Connection,
    message: QueryResponse,
    handle: Handle,
    tpe: String,
    context: JValue,
    channelId: ChannelId
  ): Unit = {
    val responseData = QueryService.ResponseData(None, Some(connection.iid), tpe, None, message.results)
    AsyncResponse(AsyncResponseType.Query, handle, true, responseData.toJson, context)
      .send(channelId)
  }

  object ResponseData extends FromJsonCapable[ResponseData]

  case class ResponseData(
    aliasIid: Option[InternalId],
    connectionIid: Option[InternalId],
    @Json("type") tpe: String,
    action: Option[StandingQueryAction],
    results: JValue
  ) extends ToJsonCapable
}
