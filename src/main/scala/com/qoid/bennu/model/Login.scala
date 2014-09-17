package com.qoid.bennu.model

import com.qoid.bennu.FromJsonCapable
import com.qoid.bennu.JsonAssist._
import com.qoid.bennu.ToJsonCapable
import com.qoid.bennu.mapper.BennuMappedInstance
import com.qoid.bennu.mapper.BennuMapperCompanion
import com.qoid.bennu.model.id.AgentId
import com.qoid.bennu.model.id.AuthenticationId
import com.qoid.bennu.model.id.InternalId
import m3.jdbc.mapper.PrimaryKey
import net.model3.chrono.DateTime

object Login extends BennuMapperCompanion[Login] with FromJsonCapable[Login]

case class Login(
  aliasIid: InternalId,
  authenticationId: AuthenticationId,
  passwordHash: String,
  agentId: AgentId = AgentId(""),
  @PrimaryKey iid: InternalId = InternalId.random,
  data: JValue = JNothing,
  created: DateTime = new DateTime,
  modified: DateTime = new DateTime,
  @transient createdByConnectionIid: InternalId = InternalId(""),
  @transient modifiedByConnectionIid: InternalId = InternalId("")
) extends BennuMappedInstance[Login] with ToJsonCapable {

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
