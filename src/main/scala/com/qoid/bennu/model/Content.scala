package com.qoid.bennu.model

import com.qoid.bennu.FromJsonCapable
import com.qoid.bennu.JdbcAssist._
import com.qoid.bennu.JsonAssist._
import com.qoid.bennu.ToJsonCapable
import com.qoid.bennu.model.id._
import com.qoid.bennu.security.AgentView
import com.qoid.bennu.squery.StandingQueryAction
import m3.Txn
import m3.jdbc._
import m3.jdbc.mapper.PrimaryKey
import m3.predef._
import net.model3.chrono.DateTime

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

    labelIids.foreach(iid => av.insert[LabeledContent](LabeledContent(instance.iid, iid)))

    Content.notifyStandingQueries(instance, StandingQueryAction.Insert)

    instance
  }

  override protected def preDelete(instance: Content): Content = {
    val av = inject[AgentView]

    av.select[LabeledContent](sql"contentIid = ${instance.iid}").foreach(av.delete[LabeledContent])

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
  created: DateTime = new DateTime,
  modified: DateTime = new DateTime,
  createdByAliasIid: InternalId = InternalId(""),
  modifiedByAliasIid: InternalId = InternalId("")
) extends HasInternalId with BennuMappedInstance[Content] { self =>
  
  type TInstance = Content
  
  def mapper = Content

  override def copy2(
    iid: InternalId = self.iid,
    agentId: AgentId = self.agentId,
    data: JValue = self.data,
    created: DateTime = self.created,
    modified: DateTime = self.modified,
    createdByAliasIid: InternalId = self.createdByAliasIid,
    modifiedByAliasIid: InternalId = self.modifiedByAliasIid
  ) = {
    copy(
      iid = iid,
      agentId = agentId,
      data = data,
      created = created,
      modified = modified,
      createdByAliasIid = createdByAliasIid,
      modifiedByAliasIid = modifiedByAliasIid
    )
  }
}

