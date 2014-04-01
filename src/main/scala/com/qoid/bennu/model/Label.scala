package com.qoid.bennu.model

import com.qoid.bennu.JdbcAssist._
import com.qoid.bennu.JsonAssist._
import com.qoid.bennu.model.id._
import com.qoid.bennu.security.AgentView
import m3.Txn
import m3.jdbc._
import m3.predef._

object Label extends BennuMapperCompanion[Label] {
  override protected def postInsert(instance: Label): Label = {
    val av = inject[AgentView]

    val parentIid = Txn.find[InternalId](LabelChild.parentIidAttrName, false)

    parentIid.foreach { iid =>
      av.insert[LabelChild](LabelChild(iid, instance.iid, instance.agentId))
    }

    instance
  }

  override protected def preDelete(instance: Label): Label = {
    val av = inject[AgentView]

    av.select[LabelAcl](sql"labelIid = ${instance.iid}").foreach(av.delete[LabelAcl])
    av.select[LabelChild](sql"parentIid = ${instance.iid} or childIid = ${instance.iid}").foreach(av.delete[LabelChild])
    av.select[LabeledContent](sql"labelIid = ${instance.iid}").foreach(av.delete[LabeledContent])

    instance
  }
}

case class Label(
  name: String,
  agentId: AgentId = AgentId(""),
  @PrimaryKey iid: InternalId = InternalId.random,
  data: JValue = JNothing,
  deleted: Boolean = false
) extends HasInternalId with BennuMappedInstance[Label] { self =>
  
  type TInstance = Label
  
  def mapper = Label
  
  override def copy2(
      iid: InternalId = self.iid, 
      agentId: AgentId = self.agentId, 
      data: JValue = self.data, 
      deleted: Boolean = self.deleted
  ) = {
    copy(iid = iid, agentId = agentId, data = data, deleted = deleted)
  }
}
