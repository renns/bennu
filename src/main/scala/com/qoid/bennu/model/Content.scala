package com.qoid.bennu.model

import com.qoid.bennu.JdbcAssist._
import m3.jdbc.PrimaryKey
import net.liftweb.json._

object Content extends BennuMapperCompanion[Content] {
}

case class Content(
  @PrimaryKey iid: InternalId = InternalId.random,
  agentId: AgentId,
  aliasIid: InternalId,
  contentType: String,
  data: JValue = JNothing,
  deleted: Boolean = false
) extends HasInternalId with BennuMappedInstance[Content] { self =>
  
  type TInstance = Content
  
  def mapper = Content
  
  override def copy2(
      iid: InternalId = self.iid, 
      agentId: AgentId = self.agentId, 
      data: JValue = self.data, 
      deleted: Boolean = self.deleted
  ) = {
    copy(iid = iid, agentId = agentId, data = data, deleted = deleted)
  }
}

