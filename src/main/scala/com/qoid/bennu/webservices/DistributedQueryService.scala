package com.qoid.bennu.webservices

import com.google.inject.Inject
import com.qoid.bennu.ErrorCode
import com.qoid.bennu.JsonAssist._
import com.qoid.bennu.JsonAssist.jsondsl._
import com.qoid.bennu.SecurityContext.AgentCapableSecurityContext
import com.qoid.bennu.ToJsonCapable
import com.qoid.bennu.distributed.DistributedManager
import com.qoid.bennu.distributed.DistributedRequestKind
import com.qoid.bennu.model._
import com.qoid.bennu.squery.StandingQueryManager2
import java.sql.{ Connection => JdbcConn }
import m3.json.Json
import m3.predef._
import m3.servlet.beans.Parm
import m3.servlet.longpoll.ChannelId
import net.model3.lang.TimeDuration
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.TimeoutException
import scala.util.Failure
import scala.util.Success
import com.qoid.bennu.SecurityContext
import net.codingwell.scalaguice.InjectorExtensions.ScalaInjector
import net.model3.lang.ThreadScheduler
import m3.Txn
import com.qoid.bennu.JdbcAssist
import com.qoid.bennu.squery.ast.Query
import com.qoid.bennu.squery.ast.Transformer
import com.qoid.bennu.squery.ast.ContentQuery
import m3.servlet.longpoll.Channel
import com.qoid.bennu.JsonAssist._
import m3.servlet.beans.MultiRequestHandler.MethodInvocationContext
import com.qoid.bennu.distributed.QueryHandler
import com.qoid.bennu.FromJsonCapable

case class DistributedQueryService @Inject()(
  injector: ScalaInjector,
  distributedMgr: DistributedManager,
  sQueryMgr: StandingQueryManager2,
  securityContext: AgentCapableSecurityContext,
  channelId: ChannelId,
  channel: Channel,
  methodContext: Option[MethodInvocationContext],
  @Parm("type") _type: String,
  @Parm("q") queryStr: String,
  @Parm aliasIids: List[InternalId] = Nil,
  @Parm connectionIids: List[InternalId] = Nil,
  @Parm leaveStanding: Boolean = false,
  @Parm timeout: TimeDuration = new TimeDuration("5 seconds"),
  @Parm context: Option[JValue] = None
) extends Logging {

  import DistributedQueryService._
  
  implicit def jdbcConn = injector.instance[JdbcConn]
  
  lazy val threadScheduler = injector.instance[ThreadScheduler]
  
  val resolvedContext = context.orElse(methodContext.map(_.value)).getOrElse(JNothing)
  val handle = InternalId.random
  val requestData = RequestData(_type, queryStr, leaveStanding, handle, resolvedContext)
  val requestDataJson = requestData.toJson
  
  def submitLocalAgentQuery(sc: AgentCapableSecurityContext) = {
    
    def localAgentQuery(sc: AgentCapableSecurityContext) = Txn {
      Txn.setViaTypename[SecurityContext](sc)
      val results = QueryHandler.process(requestData, injector)
      val data = ResponseData(Some(sc.aliasIid), None, _type, Some(results), resolvedContext)
      AsyncResponse(AsyncResponseType.Query, handle, true, data.toJson).send(channelId)
    }

    threadScheduler.submit(s"localAgentQuery-${sc}", () => localAgentQuery(sc))
    
    // register a standing query
    
    
  }
  
  def service: JValue = {
    
    // if we didn't get any connections or aliases to query on then query using the implicit security context 
    if ( aliasIids.isEmpty && connectionIids.isEmpty ) {
      submitLocalAgentQuery(securityContext)
    }
    
    for (aliasIid <- aliasIids) {
      submitLocalAgentQuery(SecurityContext.AliasSecurityContext(aliasIid))
    }
    
    for (connectionIid <- connectionIids) {
      
      if (leaveStanding) {
        sQueryMgr.add(securityContext.agentId, handle, connectionIid, channelId, _type)
      }

      distributedMgr.sendRequest(
        connectionIid,
        DistributedRequestKind.Query,
        requestDataJson,
        timeout
      ).onComplete {
        case Success(results) =>
          val data = ResponseData(None, Some(connectionIid), _type, Some(results), resolvedContext)
          AsyncResponse(AsyncResponseType.Query, handle, true, data.toJson).send(channelId)
        case Failure(t: TimeoutException) =>
          val data = ResponseData(None, Some(connectionIid), _type, None, resolvedContext)
          val error = AsyncResponseError(ErrorCode.Timeout, t.getMessage)
          AsyncResponse(AsyncResponseType.Query, handle, false, data.toJson, Some(error)).send(channelId)
          logger.debug(s"distributed query: timed out after $timeout milliseconds")
        case Failure(t) =>
          val data = ResponseData(None, Some(connectionIid), _type, None, resolvedContext)
          val error = AsyncResponseError(ErrorCode.Generic, t.getMessage, Some(t.getStackTraceString))
          AsyncResponse(AsyncResponseType.Query, handle, false, data.toJson, Some(error)).send(channelId)
          logger.warn("distributed query: FAIL", t)
      }
    }

    "handle" -> handle
  }
}

object DistributedQueryService {

  object RequestData extends FromJsonCapable[RequestData]
  
  case class RequestData(
    @Json("type") tpe: String,
    query: String,
    leaveStanding: Boolean,
    handle: InternalId,
    context: JValue    
  ) extends ToJsonCapable

  case class ResponseData(
    aliasIid: Option[InternalId],
    connectionIid: Option[InternalId],
    @Json("type") tpe: String,
    results: Option[JValue] = None,
    context: JValue    
  ) extends ToJsonCapable
  
}
