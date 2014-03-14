package com.qoid.bennu.webservices

import com.google.inject.Inject
import com.qoid.bennu.FromJsonCapable
import com.qoid.bennu.JdbcAssist._
import com.qoid.bennu.JsonAssist._
import com.qoid.bennu.JsonAssist.jsondsl._
import com.qoid.bennu.ToJsonCapable
import com.qoid.bennu.distributed.DistributedManager
import com.qoid.bennu.distributed.QueryResponseManager
import com.qoid.bennu.distributed.messages._
import com.qoid.bennu.model._
import com.qoid.bennu.security.AgentView
import com.qoid.bennu.security.AliasSecurityContext
import com.qoid.bennu.security.SecurityContext
import com.qoid.bennu.squery.StandingQueryAction
import com.qoid.bennu.squery.StandingQueryManager
import java.sql.{ Connection => JdbcConn }
import m3.Txn
import m3.jdbc._
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
  securityContext: SecurityContext,
  channelId: ChannelId,
  @Parm("type") _type: String,
  @Parm("q") queryStr: String,
  @Parm aliasIid: Option[InternalId] = None,
  @Parm connectionIids: List[InternalId] = Nil,
  @Parm historical: Boolean = true,
  @Parm standing: Boolean = false,
  @Parm context: JValue = JNothing
) extends Logging {

  implicit def jdbcConn = injector.instance[JdbcConn]
  val handle = Handle.random

  def service: JValue = {

    val sc = aliasIid match {
      case Some(iid) => AliasSecurityContext(injector, iid)
      case _ => securityContext
    }

    Txn.setViaTypename[SecurityContext](sc)

    if (aliasIid.nonEmpty || connectionIids.isEmpty) submitLocalQuery(sc)
    if (connectionIids.nonEmpty) submitRemoteQuery()

    "handle" -> handle
  }

  def submitLocalQuery(sc: SecurityContext): Unit = {
    if (historical) {
      future {
        Txn {
          Txn.setViaTypename[SecurityContext](sc)
          val av = injector.instance[AgentView]
          implicit val mapper = findMapperByTypeName(_type)
          val results = av.select(queryStr).toList
          val responseData = QueryService.ResponseData(Some(sc.aliasIid), None, _type, None, results.map(_.toJson))
          AsyncResponse(AsyncResponseType.Query, handle, true, responseData.toJson, context)
            .send(channelId)
        }
      }
    }

    if (standing) {
      sQueryMgr.addLocal(
        sc.agentId,
        sc.aliasIid,
        handle,
        _type,
        queryStr,
        QueryService.sQueryResponseHandler(_, _, sc.aliasIid, handle, _type, context, channelId)
      )
    }
  }

  def submitRemoteQuery(): Unit = {
    val av = injector.instance[AgentView]
    val request = QueryRequest(_type, queryStr, historical, standing, handle)

    injector.instance[QueryResponseManager].registerHandle(
      handle,
      QueryService.distributedResponseHandler(_, _, handle, _type, context, channelId)
    )

    connectionIids.foreach { connectionIid =>
      av.select[Connection](sql"iid = $connectionIid").foreach { c =>
        distributedMgr.send(c, DistributedMessage(DistributedMessageKind.QueryRequest, 1, request.toJson))
      }
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
    val responseData = QueryService.ResponseData(Some(aliasIid), None, tpe, Some(action), List(instance.toJson))
    AsyncResponse(AsyncResponseType.SQuery, handle, true, responseData.toJson, context)
      .send(channelId)
  }

  def distributedResponseHandler(
    connection: Connection,
    message: QueryResponse,
    handle: Handle,
    tpe: String,
    context: JValue,
    channelId: ChannelId
  ): Unit = {
    val responseData = QueryService.ResponseData(None, Some(connection.iid), tpe, message.action, message.results)
    val responseType = if (message.standing) AsyncResponseType.SQuery else AsyncResponseType.Query
    AsyncResponse(responseType, handle, true, responseData.toJson, context)
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
