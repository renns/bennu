package com.qoid.bennu.model

import com.qoid.bennu.JdbcAssist._
import m3.jdbc.PrimaryKey
import net.liftweb.json._

object Connection extends BennuMapperCompanion[Connection] {
}

case class Connection(
  agentId: AgentId,
  aliasIid: InternalId,
  localPeerId: PeerId,
  remotePeerId: PeerId,
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

