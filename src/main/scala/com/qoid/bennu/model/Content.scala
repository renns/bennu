package com.qoid.bennu.model

import com.qoid.bennu.FromJsonCapable
import com.qoid.bennu.ToJsonCapable
import com.qoid.bennu.mapper.BennuMappedInstance
import com.qoid.bennu.mapper.BennuMapperCompanion
import com.qoid.bennu.model.id.AgentId
import com.qoid.bennu.model.id.InternalId
import com.qoid.bennu.model.id.SharedId
import com.qoid.bennu.query.ast.ContentQuery
import com.qoid.bennu.query.ast.Node
import m3.Chord
import m3.jdbc._
import net.liftweb.json._
import net.model3.chrono.DateTime

object Content extends BennuMapperCompanion[Content] with FromJsonCapable[Content] {

  override protected val queryTransformer: PartialFunction[Node, Chord] = ContentQuery.transformer

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
}

case class Content(
  contentType: String,
  @PrimaryKey iid: InternalId = InternalId.random,
  agentId: AgentId = AgentId(""),
  data: JValue = JNothing,
  metaData: JValue = JNothing,
  created: DateTime = new DateTime,
  modified: DateTime = new DateTime,
  createdByConnectionIid: InternalId = InternalId(""),
  modifiedByConnectionIid: InternalId = InternalId("")
) extends BennuMappedInstance[Content] with ToJsonCapable {

  override def copy2(
    agentId: AgentId = agentId,
    created: DateTime = created,
    modified: DateTime = modified,
    createdByConnectionIid: InternalId = createdByConnectionIid,
    modifiedByConnectionIid: InternalId = modifiedByConnectionIid
  ) = {
    copy(
      agentId = agentId,
      created = created,
      modified = modified,
      createdByConnectionIid = createdByConnectionIid,
      modifiedByConnectionIid = modifiedByConnectionIid
    )
  }
}

