package com.qoid.bennu.model

import com.qoid.bennu.JdbcAssist._
import com.qoid.bennu.JsonAssist._
import com.qoid.bennu.JsonAssist.jsondsl._
import com.qoid.bennu.model.id._
import com.qoid.bennu.security.AgentView
import m3.Txn
import m3.jdbc.PrimaryKey
import m3.predef._

object Alias extends BennuMapperCompanion[Alias] {
  private val aliasLabelColor = "#000000"
  private val metaLabelColor = "#7FBA00"

  val metaLabelName = "Meta"
  val verificationsLabelName = "Verifications"
  val connectionsLabelName = "Connections"

  override protected def preInsert(instance: Alias): Alias = {
    val av = inject[AgentView]

    val label = av.insert[Label](Label(instance.name, instance.agentId, data = "color" -> aliasLabelColor))

    instance.copy(rootLabelIid = label.iid)
  }

  override protected def postInsert(instance: Alias): Alias = {
    val av = inject[AgentView]

    Txn {
      Txn.set(LabelChild.parentIidAttrName, instance.rootLabelIid)
      val metaLabel = av.insert[Label](Label(metaLabelName, instance.agentId, data = "color" -> metaLabelColor))

      Txn {
        Txn.set(LabelChild.parentIidAttrName, metaLabel.iid)
        av.insert[Label](Label(connectionsLabelName, instance.agentId, data = "color" -> metaLabelColor))
        av.insert[Label](Label(verificationsLabelName, instance.agentId, data = "color" -> metaLabelColor))
      }
    }

    val profileName = Txn.find[String](Profile.nameAttrName, false).getOrElse(instance.name)
    val profileImgSrc = Txn.find[String](Profile.imgSrcAttrName, false).getOrElse("")
    av.insert[Profile](Profile(instance.iid, profileName, profileImgSrc, instance.agentId))

    instance
  }

  override protected def postUpdate(instance: Alias): Alias = {
    val av = inject[AgentView]

    val label = av.fetch[Label](instance.rootLabelIid)
    av.update(label.copy(name = instance.name))

    instance
  }
}

case class Alias(
  name: String,
  agentId: AgentId = AgentId(""),
  rootLabelIid: InternalId = InternalId(""),
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
}
