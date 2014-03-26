package com.qoid.bennu.model

import com.qoid.bennu.JdbcAssist._
import com.qoid.bennu.JsonAssist._
import com.qoid.bennu.JsonAssist.jsondsl._
import com.qoid.bennu.distributed.DistributedManager
import com.qoid.bennu.model.id._
import com.qoid.bennu.security.AgentView
import java.sql.{ Connection => JdbcConn }
import m3.Txn
import m3.jdbc._
import m3.predef._

object Connection extends BennuMapperCompanion[Connection] {
  private val connectionLabelName = "connection"
  private val connectionLabelColor = "#7FBA00"

  override protected def preInsert(instance: Connection): Connection = {
    val av = inject[AgentView]
    implicit val jdbcConn = inject[JdbcConn]

    val alias = av.fetch[Alias](instance.aliasIid)
    val rootLabel = av.fetch[Label](alias.rootLabelIid)
    val metaLabel = rootLabel.findChild(Alias.metaLabelName).head
    val connectionsLabel = metaLabel.findChild(Alias.connectionsLabelName).head

    Txn {
      Txn.set(LabelChild.parentIidAttrName, connectionsLabel.iid)
      val label = av.insert[Label](Label(connectionLabelName, instance.agentId, data = "color" -> connectionLabelColor))
      instance.copy(metaLabelIid = label.iid)
    }
  }

  override protected def postInsert(instance: Connection): Connection = {
    inject[DistributedManager].listen(instance)
    instance
  }

  override protected def postDelete(instance: Connection): Connection = {
    inject[DistributedManager].stopListen(instance)
    instance
  }
}

case class Connection(
  aliasIid: InternalId,
  localPeerId: PeerId,
  remotePeerId: PeerId,
  agentId: AgentId = AgentId(""),
  metaLabelIid: InternalId = InternalId(""),
  @PrimaryKey iid: InternalId = InternalId.random,
  data: JValue = JNothing,
  deleted: Boolean = false
) extends HasInternalId with BennuMappedInstance[Connection] { self =>
  
  type TInstance = Connection
  
  def mapper = Connection
  
  override def copy2(
      iid: InternalId = self.iid, 
      agentId: AgentId = self.agentId, 
      data: JValue = self.data, 
      deleted: Boolean = self.deleted
  ) = {
    copy(iid = iid, agentId = agentId, data = data, deleted = deleted)
  }
}
