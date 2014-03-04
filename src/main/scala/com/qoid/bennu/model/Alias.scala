package com.qoid.bennu.model

import com.qoid.bennu.JdbcAssist._
import com.qoid.bennu.squery.StandingQueryAction
import m3.jdbc.PrimaryKey
import net.liftweb.json._

object Alias extends BennuMapperCompanion[Alias] {
}

case class Alias(
  rootLabelIid: InternalId,
  profile: JValue,
  agentId: AgentId = AgentId(""),
  @PrimaryKey iid: InternalId = InternalId.random,
  data: JValue = JNothing,
  deleted: Boolean = false
) extends HasInternalId with BennuMappedInstance[Alias] { self =>
  
  type TInstance = Alias
  
  def mapper = Alias
  
  override def copy2(
      iid: InternalId = self.iid, 
      agentId: AgentId = self.agentId, 
      data: JValue = self.data, 
      deleted: Boolean = self.deleted
  ) = {
    copy(iid = iid, agentId = agentId, data = data, deleted = deleted)
  }

  // TODO: This whole method can be removed once we have proper distributed standing queries
  // This method is used as a hack to send profile squery events
  override def notifyStandingQueries(action: StandingQueryAction): Alias = {
    import com.qoid.bennu.JsonAssist.jsondsl._
    import com.qoid.bennu.squery._
    import java.sql.{ Connection => JdbcConn }
    import m3.jdbc._
    import m3.predef._

    val sQueryMgr = inject[StandingQueryManager]
    implicit val jdbcConn = inject[JdbcConn]

    SqueryEvalThread.enqueue(action, this)

    val connections = Connection.select(sql"aliasIid = $iid")

    for (connection <- connections) {
      val remoteConnection = Connection.selectOne(sql"localPeerId = ${connection.remotePeerId} and remotePeerId = ${connection.localPeerId}")

      val queries = sQueryMgr.get(remoteConnection.agentId, "profile")

      for {
        query <- queries
      } {
        val data = ("connectionIid" -> remoteConnection.iid) ~ ("profile" -> profile)
        val event = StandingQueryEvent(action, "profile", data)
        query._1.listener(event)
      }
    }

    this
  }
}
