package com.qoid.bennu.model

import com.qoid.bennu.JdbcAssist._
import com.qoid.bennu.model.introduction.IntroductionState
import m3.jdbc.PrimaryKey
import net.liftweb.json._

object Introduction extends BennuMapperCompanion[Introduction] {
}

case class Introduction(
  aConnectionIid: InternalId,
  aState: IntroductionState,
  bConnectionIid: InternalId,
  bState: IntroductionState,
  agentId: AgentId = AgentId(""),
  @PrimaryKey iid: InternalId = InternalId.random,
  data: JValue = JNothing,
  deleted: Boolean = false
) extends HasInternalId with BennuMappedInstance[Introduction] { self =>

  type TInstance = Introduction

  def mapper = Introduction

  override def copy2(
    iid: InternalId = self.iid,
    agentId: AgentId = self.agentId,
    data: JValue = self.data,
    deleted: Boolean = self.deleted
  ) = {
    copy(iid = iid, agentId = agentId, data = data, deleted = deleted)
  }
}
