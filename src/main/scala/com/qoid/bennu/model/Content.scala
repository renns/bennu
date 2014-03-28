package com.qoid.bennu.model

import com.qoid.bennu.FromJsonCapable
import com.qoid.bennu.JdbcAssist._
import com.qoid.bennu.JsonAssist._
import com.qoid.bennu.ToJsonCapable
import com.qoid.bennu.model.id._
import com.qoid.bennu.security.AgentView
import m3.jdbc.PrimaryKey
import m3.predef._
import m3.Txn

object Content extends BennuMapperCompanion[Content] {
  object MetaData extends FromJsonCapable[MetaData]

  case class MetaData(
    verifiedContent: Option[MetaDataVerifiedContent] = None,
    verifications: Option[List[MetaDataVerification]] = None
  ) extends ToJsonCapable

  case class MetaDataVerifiedContent(
    hash: JValue,
    hashAlgorithm: String
  )

  case class MetaDataVerification(
    verifierId: SharedId,
    verificationIid: InternalId,
    hash: JValue,
    hashAlgorithm: String
  )

  override protected def postInsert(instance: Content): Content = {
    val av = inject[AgentView]

    val labelIids = Txn.find[List[InternalId]](LabeledContent.labelIidsAttrName, false).getOrElse(Nil)

    labelIids.foreach { iid =>
      av.insert[LabeledContent](LabeledContent(instance.iid, iid, agentId = instance.agentId))
    }

    instance
  }
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

