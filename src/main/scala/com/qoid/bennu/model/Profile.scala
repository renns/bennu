package com.qoid.bennu.model

import com.qoid.bennu.JdbcAssist._
import com.qoid.bennu.JsonAssist._
import com.qoid.bennu.model.id._
import m3.jdbc.PrimaryKey
import net.model3.chrono.DateTime

object Profile extends BennuMapperCompanion[Profile] {
  val nameAttrName = "com.qoid.bennu.model.Profile.name"
  val imgSrcAttrName = "com.qoid.bennu.model.Profile.imgSrc"
}

case class Profile(
  aliasIid: InternalId,
  name: String,
  imgSrc: String,
  agentId: AgentId = AgentId(""),
  @PrimaryKey iid: InternalId = InternalId.random,
  sharedId: SharedId = SharedId.random,
  data: JValue = JNothing,
  created: DateTime = new DateTime,
  modified: DateTime = new DateTime,
  createdByConnectionIid: InternalId = InternalId(""),
  modifiedByConnectionIid: InternalId = InternalId("")
) extends HasInternalId with BennuMappedInstance[Profile] { self =>

  type TInstance = Profile

  def mapper = Profile

  override def copy2(
    iid: InternalId = self.iid,
    agentId: AgentId = self.agentId,
    data: JValue = self.data,
    created: DateTime = self.created,
    modified: DateTime = self.modified,
    createdByConnectionIid: InternalId = self.createdByConnectionIid,
    modifiedByConnectionIid: InternalId = self.modifiedByConnectionIid
  ) = {
    copy(
      iid = iid,
      agentId = agentId,
      data = data,
      created = created,
      modified = modified,
      createdByConnectionIid = createdByConnectionIid,
      modifiedByConnectionIid = modifiedByConnectionIid
    )
  }
}
