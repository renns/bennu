package com.qoid.bennu.model

import com.qoid.bennu.JdbcAssist._
import com.qoid.bennu.distributed.DistributedManager
import com.qoid.bennu.model.id._
import m3.jdbc.PrimaryKey
import m3.predef._
import net.liftweb.json._

object Connection extends BennuMapperCompanion[Connection] {
}

case class Connection(
  aliasIid: InternalId,
  metaLabelIid: InternalId,
  localPeerId: PeerId,
  remotePeerId: PeerId,
  agentId: AgentId = AgentId(""),
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

  override def postInsert(): Unit = {
    inject[DistributedManager].listen(this)
  }

  override def postDelete(): Unit = {
    inject[DistributedManager].stopListen(this)
  }
}
