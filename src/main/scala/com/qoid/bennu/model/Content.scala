package com.qoid.bennu.model

import com.qoid.bennu.FromJsonCapable
import com.qoid.bennu.JsonAssist._
import com.qoid.bennu.ToJsonCapable
import com.qoid.bennu.mapper.BennuMappedInstance
import com.qoid.bennu.mapper.BennuMapperCompanion
import com.qoid.bennu.model.id.AgentId
import com.qoid.bennu.model.id.InternalId
import com.qoid.bennu.model.id.SemanticId
import com.qoid.bennu.query.ast.ContentQuery
import com.qoid.bennu.query.ast.Node
import m3.Chord
import m3.jdbc.mapper.PrimaryKey
import net.model3.chrono.DateTime

object Content extends BennuMapperCompanion[Content] with FromJsonCapable[Content] {
  override protected val queryTransformer: PartialFunction[Node, Chord] = ContentQuery.transformer
}

case class Content(
  contentType: String,
  semanticId: Option[SemanticId],
  @PrimaryKey iid: InternalId = InternalId.random,
  agentId: AgentId = AgentId(""),
  data: JValue = JNothing,
  metaData: JValue = JNothing,
  created: DateTime = new DateTime,
  modified: DateTime = new DateTime,
  @transient createdByConnectionIid: InternalId = InternalId(""),
  @transient modifiedByConnectionIid: InternalId = InternalId("")
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
