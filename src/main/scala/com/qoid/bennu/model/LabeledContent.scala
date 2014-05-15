package com.qoid.bennu.model

import com.qoid.bennu.JdbcAssist._
import com.qoid.bennu.JsonAssist._
import com.qoid.bennu.model.id._
import com.qoid.bennu.security.AgentView
import com.qoid.bennu.squery.StandingQueryAction
import m3.jdbc.PrimaryKey
import m3.predef._
import net.model3.chrono.DateTime

object LabeledContent extends BennuMapperCompanion[LabeledContent] {
  val labelIidsAttrName = "com.qoid.bennu.model.LabeledContent.labelIids"

  override protected def postInsert(instance: LabeledContent): LabeledContent = {
    val av = inject[AgentView]

    av.fetchOpt[Content](instance.contentIid).foreach { content =>
      Content.notifyStandingQueries(content, StandingQueryAction.Update)
    }

    instance
  }

  override protected def postDelete(instance: LabeledContent): LabeledContent = {
    val av = inject[AgentView]

    av.fetchOpt[Content](instance.contentIid).foreach { content =>
      Content.notifyStandingQueries(content, StandingQueryAction.Update)
    }

    instance
  }
}

case class LabeledContent(
  contentIid: InternalId,
  labelIid: InternalId,
  @PrimaryKey iid: InternalId = InternalId.random,
  agentId: AgentId = AgentId(""),
  data: JValue = JNothing,
  created: DateTime = new DateTime,
  modified: DateTime = new DateTime,
  createdByAliasIid: InternalId = InternalId(""),
  modifiedByAliasIid: InternalId = InternalId("")
) extends HasInternalId with BennuMappedInstance[LabeledContent] { self =>
  
  type TInstance = LabeledContent
  
  def mapper = LabeledContent

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
