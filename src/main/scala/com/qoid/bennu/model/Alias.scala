package com.qoid.bennu.model

import com.qoid.bennu.JdbcAssist._
import com.qoid.bennu.JsonAssist._
import m3.jdbc.PrimaryKey

object Alias extends BennuMapperCompanion[Alias] {
}

case class Alias(
  rootLabelIid: InternalId,
  profile: JValue,
  agentId: AgentId = AgentId(""),
  @PrimaryKey iid: InternalId = InternalId.random,
  data: JValue = JNothing,
  deleted: Boolean = false
) extends HasInternalId with BennuMappedInstance[Alias] { self =>
  
  type TInstance = Alias
  
  def mapper = Alias
  
  override def copy2(
      iid: InternalId = self.iid, 
      agentId: AgentId = self.agentId, 
      data: JValue = self.data, 
      deleted: Boolean = self.deleted
  ) = {
    copy(iid = iid, agentId = agentId, data = data, deleted = deleted)
  }

  //TODO: Remove below after profiles are re-done
  import com.qoid.bennu.squery.StandingQueryAction
  override def notifyStandingQueries(action: StandingQueryAction): Unit = {
    super.notifyStandingQueries(action)

    if (action == StandingQueryAction.Update) {
      Profile.fromAlias(this).notifyStandingQueries(action)
    }
  }
}
