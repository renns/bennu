package com.qoid.bennu.webservices

import com.google.inject.Inject
import com.qoid.bennu.JdbcAssist._
import com.qoid.bennu.JsonAssist._
import com.qoid.bennu.JsonAssist.jsondsl._
import com.qoid.bennu.distributed.DistributedManager
import com.qoid.bennu.distributed.QueryResponseManager
import com.qoid.bennu.distributed.messages
import com.qoid.bennu.distributed.messages.DistributedMessage
import com.qoid.bennu.distributed.messages.DistributedMessageKind
import com.qoid.bennu.distributed.messages.QueryRequest
import com.qoid.bennu.model._
import com.qoid.bennu.model.id._
import com.qoid.bennu.security.AgentView
import com.qoid.bennu.security.AliasSecurityContext
import com.qoid.bennu.security.SecurityContext
import com.qoid.bennu.squery.StandingQueryAction
import com.qoid.bennu.squery.StandingQueryManager
import java.sql.{ Connection => JdbcConn }
import m3.Txn
import m3.jdbc._
import m3.predef._
import m3.servlet.beans.Parm
import m3.servlet.longpoll.ChannelId
import m3.servlet.longpoll.ChannelManager
import net.codingwell.scalaguice.InjectorExtensions.ScalaInjector
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.future

case class QueryService @Inject()(
  injector: ScalaInjector,
  distributedMgr: DistributedManager,
  sQueryMgr: StandingQueryManager,
  queryResponseMgr: QueryResponseManager,
  channelMgr: ChannelManager,
  securityContext: SecurityContext,
  channelId: ChannelId,
  @Parm("type") _type: String,
  @Parm("q") queryStr: String,
  @Parm aliasIid: Option[InternalId] = None,
  @Parm local: Boolean = true,
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

    if (local) submitLocalQuery(sc)
    if (connectionIids.nonEmpty) submitRemoteQuery(sc)

    ("handle" -> handle) ~ ("context" -> context)
  }

  private def submitLocalQuery(sc: SecurityContext): Unit = {
    if (historical) {
      future {
        Txn {
          Txn.setViaTypename[SecurityContext](sc)
          val av = injector.instance[AgentView]
          implicit val mapper = findMapperByTypeName(_type)
          val results = av.select(queryStr).toList
          val response = QueryResponse(QueryResponseType.Query, handle, _type, context, results.map(_.toJson), Some(sc.aliasIid))
          channelMgr.channel(channelId).put(response.toJson)
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
        QueryService.sQueryResponseHandler(_, _, sc.aliasIid, handle, _type, context, channelMgr, channelId)
      )
    }
  }

  private def submitRemoteQuery(sc: SecurityContext): Unit = {
    val av = injector.instance[AgentView]
    val request = QueryRequest(_type, queryStr, historical, standing, handle)

    if (standing) {
      sQueryMgr.addConnectionIids(handle, connectionIids, sc.aliasIid)
    }

    queryResponseMgr.registerHandle(
      handle,
      QueryService.distributedResponseHandler(_, _, handle, _type, context, channelMgr, channelId)
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
    channelMgr: ChannelManager,
    channelId: ChannelId
  ): Unit = {
    val response = QueryResponse(QueryResponseType.SQuery, handle, tpe, context, List(instance.toJson), Some(aliasIid), None, Some(action))
    channelMgr.channel(channelId).put(response.toJson)
  }

  def distributedResponseHandler(
    connection: Connection,
    message: messages.QueryResponse,
    handle: Handle,
    tpe: String,
    context: JValue,
    channelMgr: ChannelManager,
    channelId: ChannelId
  ): Unit = {
    val responseType = if (message.standing) QueryResponseType.SQuery else QueryResponseType.Query
    val response = QueryResponse(responseType, handle, tpe, context, message.results, None, Some(connection.iid), message.action)
    channelMgr.channel(channelId).put(response.toJson)
  }
}
