package com.qoid.bennu.model

import com.qoid.bennu.FromJsonCapable
import com.qoid.bennu.JdbcAssist._
import com.qoid.bennu.JsonAssist._
import com.qoid.bennu.ToJsonCapable
import com.qoid.bennu.model.id._
import m3.jdbc.PrimaryKey

object Content extends BennuMapperCompanion[Content] {
  object MetaData extends FromJsonCapable[MetaData]

  case class MetaData(
    verifiedContent: Option[MetaDataVerifiedContent] = None,
    verifications: Option[List[MetaDataVerification]] = None
  ) extends ToJsonCapable

  case class MetaDataVerifiedContent(
    hash: String,
    hashAlgorithm: String
  )

  case class MetaDataVerification(
    verifierId: SharedId,
    verificationIid: InternalId,
    hash: String,
    hashAlgorithm: String
  )
}

case class Content(
  aliasIid: InternalId,
  contentType: String,
  @PrimaryKey iid: InternalId = InternalId.random,
  agentId: AgentId = AgentId(""),
  data: JValue = JNothing,
  metaData: JValue = JNothing,
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

