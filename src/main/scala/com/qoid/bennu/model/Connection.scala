package com.qoid.bennu.model

import com.qoid.bennu.JdbcAssist._
import com.qoid.bennu.JsonAssist._
import com.qoid.bennu.JsonAssist.jsondsl._
import com.qoid.bennu.distributed.DistributedManager
import com.qoid.bennu.model.id._
import com.qoid.bennu.security.AgentView
import m3.Txn
import m3.jdbc._
import m3.predef._
import net.model3.chrono.DateTime

object Connection extends BennuMapperCompanion[Connection] {
  private val connectionLabelName = "connection"
  private val connectionLabelColor = "#7FBA00"

  override protected def preInsert(instance: Connection): Connection = {
    val av = inject[AgentView]
    var newInstance = instance

    val alias = av.fetch[Alias](instance.aliasIid)
    val rootLabel = av.fetch[Label](alias.rootLabelIid)

    av.findChildLabel(rootLabel.iid, Alias.metaLabelName).foreach { metaLabel =>
      av.findChildLabel(metaLabel.iid, Alias.connectionsLabelName).foreach { connectionsLabel =>
        Txn {
          Txn.set(LabelChild.parentIidAttrName, connectionsLabel.iid)
          val label = av.insert[Label](Label(connectionLabelName, data = "color" -> connectionLabelColor))
          newInstance = instance.copy(metaLabelIid = label.iid)
        }
      }
    }

    newInstance
  }

  override protected def postInsert(instance: Connection): Connection = {
    inject[DistributedManager].listen(instance)
    instance
  }

  override protected def preDelete(instance: Connection): Connection = {
    val av = inject[AgentView]

    av.select[Introduction](sql"aConnectionIid = ${instance.iid} or bConnectionIid = ${instance.iid}").foreach(av.delete[Introduction])
    av.select[LabelAcl](sql"connectionIid = ${instance.iid}").foreach(av.delete[LabelAcl])
    av.select[Notification](sql"fromConnectionIid = ${instance.iid}").foreach(av.delete[Notification])

    instance
  }

  override protected def postDelete(instance: Connection): Connection = {
    val av = inject[AgentView]

    inject[DistributedManager].stopListen(instance)

    av.delete[Label](av.fetch[Label](instance.metaLabelIid))

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
  deleted: Boolean = false,
  created: DateTime = new DateTime,
  modified: DateTime = new DateTime,
  createdByAliasIid: InternalId = InternalId(""),
  modifiedByAliasIid: InternalId = InternalId("")
) extends HasInternalId with BennuMappedInstance[Connection] { self =>
  
  type TInstance = Connection
  
  def mapper = Connection

  override def copy2(
    iid: InternalId = self.iid,
    agentId: AgentId = self.agentId,
    data: JValue = self.data,
    deleted: Boolean = self.deleted,
    created: DateTime = self.created,
    modified: DateTime = self.modified,
    createdByAliasIid: InternalId = self.createdByAliasIid,
    modifiedByAliasIid: InternalId = self.modifiedByAliasIid
  ) = {
    copy(
      iid = iid,
      agentId = agentId,
      data = data,
      deleted = deleted,
      created = created,
      modified = modified,
      createdByAliasIid = createdByAliasIid,
      modifiedByAliasIid = modifiedByAliasIid
    )
  }
}
