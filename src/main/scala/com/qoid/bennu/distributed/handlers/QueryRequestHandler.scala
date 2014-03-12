package com.qoid.bennu.distributed.handlers

import com.qoid.bennu.AgentView
import com.qoid.bennu.JdbcAssist._
import com.qoid.bennu.JsonAssist._
import com.qoid.bennu.distributed.DistributedManager
import com.qoid.bennu.distributed.messages._
import com.qoid.bennu.model.{Alias, Connection}
import m3.predef._

object QueryRequestHandler {
  def handle(connection: Connection, message: DistributedMessage): Unit = {
    message.version match {
      case 1 => process(connection, message)
      case _ => inject[DistributedManager].sendNotSupported(connection)
    }
  }

  private def process(connection: Connection, message: DistributedMessage): Unit = {
    val agentView = inject[AgentView]
    val distributedMgr = inject[DistributedManager]

    val requestData = QueryRequest.fromJson(message.data)

    // TODO: The following treats profile as a special case and it bypasses security.
    //       Once profile is re-done to be just content, we can remove this special case.
    val results = requestData.tpe match {
      case p if p =:= "profile" =>
        implicit val jdbcConn = inject[java.sql.Connection]
        JArray(List(Alias.fetch(connection.aliasIid).profile))
      case _ =>
        val mapper = findMapperByTypeName(requestData.tpe)
        JArray(agentView.select(requestData.query)(mapper).map(_.toJson).toList)
    }

    val responseData = QueryResponse(requestData.handle, results)
    val responseMessage = DistributedMessage(DistributedMessageKind.QueryResponse, 1, responseData.toJson)

    distributedMgr.send(connection, responseMessage)
  }
}
