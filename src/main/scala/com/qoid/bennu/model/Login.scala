package com.qoid.bennu.model

import com.qoid.bennu.JdbcAssist._
import com.qoid.bennu.JsonAssist._
import com.qoid.bennu.model.id._
import m3.jdbc.PrimaryKey

object Login extends BennuMapperCompanion[Login] {
}

case class Login(
  aliasIid: InternalId,
  authenticationId: AuthenticationId,
  passwordHash: String,
  salt: String,
  agentId: AgentId = AgentId(""),
  @PrimaryKey iid: InternalId = InternalId.random,
  data: JValue = JNothing,
  deleted: Boolean = false
) extends HasInternalId with BennuMappedInstance[Login] { self =>

  type TInstance = Login

  def mapper = Login

  override def copy2(
    iid: InternalId = self.iid,
    agentId: AgentId = self.agentId,
    data: JValue = self.data,
    deleted: Boolean = self.deleted
  ) = {
    copy(iid = iid, agentId = agentId, data = data, deleted = deleted)
  }
}
