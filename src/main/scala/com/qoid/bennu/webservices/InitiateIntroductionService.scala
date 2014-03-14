package com.qoid.bennu.webservices

import com.google.inject.Inject
import com.qoid.bennu.distributed.DistributedManager
import com.qoid.bennu.distributed.QueryResponseManager
import com.qoid.bennu.distributed.messages._
import com.qoid.bennu.model._
import com.qoid.bennu.security.AgentView
import com.qoid.bennu.security.SecurityContext
import java.sql.{ Connection => JdbcConn }
import m3.Txn
import m3.predef._
import m3.servlet.beans.Parm
import net.liftweb.json._
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scala.concurrent.Promise
import scala.language.existentials

case class InitiateIntroductionService @Inject()(
  injector: ScalaInjector,
  securityContext: SecurityContext,
  distributedMgr: DistributedManager,
  queryResponseMgr: QueryResponseManager,
  @Parm aConnectionIid: InternalId,
  @Parm aMessage: String,
  @Parm bConnectionIid: InternalId,
  @Parm bMessage: String
) extends Logging {

  implicit def jdbcConn = injector.instance[JdbcConn]

  def service: JValue = {
    val av = injector.instance[AgentView]

    val aConnection = av.fetch[Connection](aConnectionIid)
    val bConnection = av.fetch[Connection](bConnectionIid)

    implicit val ec = ExecutionContext.Implicits.global

    for {
      profileA <- getProfile(av, aConnection)
      profileB <- getProfile(av, bConnection)
    } {
      Txn {
        Txn.setViaTypename[SecurityContext](securityContext)

        val introduction = Introduction(aConnectionIid, IntroductionState.NotResponded, bConnectionIid, IntroductionState.NotResponded, securityContext.agentId)
        av.insert(introduction)

        distributedMgr.send(
          aConnection,
          DistributedMessage(
            DistributedMessageKind.IntroductionRequest,
            1,
            IntroductionRequest(introduction.iid, aMessage, profileB).toJson
          )
        )

        distributedMgr.send(
          bConnection,
          DistributedMessage(
            DistributedMessageKind.IntroductionRequest,
            1,
            IntroductionRequest(introduction.iid, bMessage, profileA).toJson
          )
        )
      }
    }

    JString("success")
  }

  private def getProfile(av: AgentView, connection: Connection): Future[JValue] = {
    val p = Promise[JValue]()
    val handle = Handle.random
    val request = QueryRequest("profile", "", historical = true, standing = false, handle = handle)

    queryResponseMgr.registerHandle(
      handle,
      InitiateIntroductionService.distributedResponseHandler(_, _, p)
    )

    distributedMgr.send(connection, DistributedMessage(DistributedMessageKind.QueryRequest, 1, request.toJson))

    p.future
  }
}

object InitiateIntroductionService {
  def distributedResponseHandler(
    connection: Connection,
    message: QueryResponse,
    p: Promise[JValue]
  ): Unit = {
    message.results match {
      case JArray(profile :: Nil) => p.success(profile)
      case _ => p.failure(new Exception("Invalid profile query result"))
    }
  }
}
