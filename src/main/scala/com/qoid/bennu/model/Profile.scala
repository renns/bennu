package com.qoid.bennu.model

import com.qoid.bennu.JdbcAssist._
import com.qoid.bennu.JsonAssist._
import com.qoid.bennu.JsonAssist.jsondsl._

object Profile extends BennuMapperCompanion[Profile] {
  def fromAlias(alias: Alias): Profile = Profile.fromJson(alias.profile).copy2(agentId = alias.agentId)
}

case class Profile(
  name: String,
  imgSrc: String,
  agentId: AgentId = AgentId(""),
  iid: InternalId = InternalId(""),
  data: JValue = JNothing,
  deleted: Boolean = false
) extends HasInternalId with BennuMappedInstance[Profile] { self =>

  type TInstance = Profile

  def mapper = Profile

  override def copy2(
    iid: InternalId = self.iid,
    agentId: AgentId = self.agentId,
    data: JValue = self.data,
    deleted: Boolean = self.deleted
  ) = {
    copy(iid = iid, agentId = agentId, data = data, deleted = deleted)
  }

  override def toJson: JValue = ("name" -> name) ~ ("imgSrc" -> imgSrc)
}
