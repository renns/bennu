package com.qoid.bennu.webservices

import com.google.inject.Inject
import com.qoid.bennu.JsonAssist._
import com.qoid.bennu.distributed.DistributedManager
import com.qoid.bennu.distributed.messages._
import com.qoid.bennu.model._
import com.qoid.bennu.model.id.InternalId
import com.qoid.bennu.security.AgentView
import java.sql.{ Connection => JdbcConn }
import m3.predef._
import m3.servlet.beans.Parm

case class RequestVerificationService @Inject()(
  injector: ScalaInjector,
  distributedMgr: DistributedManager,
  @Parm contentIid: InternalId,
  @Parm connectionIids: List[InternalId],
  @Parm message: String
) extends Logging {

  implicit def jdbcConn = injector.instance[JdbcConn]

  def service: JValue = {
    val av = injector.instance[AgentView]

    val content = av.fetch[Content](contentIid)
    val request = VerificationRequest(contentIid, content.contentType, content.data, message)

    connectionIids.foreach { connectionIid =>
      val connection = av.fetch[Connection](connectionIid)
      distributedMgr.send(connection, DistributedMessage(DistributedMessageKind.VerificationRequest, 1, request.toJson))
    }

    JString("success")
  }
}