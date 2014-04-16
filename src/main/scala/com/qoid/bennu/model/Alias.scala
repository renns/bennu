package com.qoid.bennu.model

import com.qoid.bennu.JdbcAssist._
import com.qoid.bennu.JsonAssist._
import com.qoid.bennu.JsonAssist.jsondsl._
import com.qoid.bennu.model.id._
import com.qoid.bennu.security.AgentView
import com.qoid.bennu.security.AuthenticationManager
import java.sql.{ Connection => JdbcConn }
import m3.Txn
import m3.jdbc._
import m3.predef._

object Alias extends BennuMapperCompanion[Alias] {
  private val aliasLabelColor = "#000000"
  private val metaLabelColor = "#7FBA00"

  val metaLabelName = "Meta"
  val verificationsLabelName = "Verifications"
  val connectionsLabelName = "Connections"

  override protected def preInsert(instance: Alias): Alias = {
    val av = inject[AgentView]

    val label = av.insert[Label](Label(instance.name, data = "color" -> aliasLabelColor))

    instance.copy(rootLabelIid = label.iid)
  }

  override protected def postInsert(instance: Alias): Alias = {
    val av = inject[AgentView]

    Txn {
      Txn.set(LabelChild.parentIidAttrName, instance.rootLabelIid)
      val metaLabel = av.insert[Label](Label(metaLabelName, data = "color" -> metaLabelColor))

      Txn {
        Txn.set(LabelChild.parentIidAttrName, metaLabel.iid)
        av.insert[Label](Label(connectionsLabelName, data = "color" -> metaLabelColor))
        av.insert[Label](Label(verificationsLabelName, data = "color" -> metaLabelColor))
      }
    }

    val profileName = Txn.find[String](Profile.nameAttrName, false).getOrElse(instance.name)
    val profileImgSrc = Txn.find[String](Profile.imgSrcAttrName, false).getOrElse("")
    av.insert[Profile](Profile(instance.iid, profileName, profileImgSrc))

    //TODO: Remove once UI supports creating logins
    Txn {
      av.selectOpt[Agent]("").foreach {
        _ =>
          val authenticationMgr = inject[AuthenticationManager]
          authenticationMgr.createLogin(instance.iid, "password")
      }
    }

    instance
  }

  override protected def postUpdate(instance: Alias): Alias = {
    val av = inject[AgentView]

    val label = av.fetch[Label](instance.rootLabelIid)
    av.update(label.copy(name = instance.name))

    //TODO: Remove once UI supports creating logins
    Txn {
      av.selectOpt[Agent]("").foreach {
        _ =>
          val authenticationMgr = inject[AuthenticationManager]
          authenticationMgr.updateAuthenticationId(instance.iid)
      }
    }

    instance
  }

  override protected def preDelete(instance: Alias): Alias = {
    val av = inject[AgentView]

    av.select[Connection](sql"aliasIid = ${instance.iid}").foreach(av.delete[Connection])
    av.select[Profile](sql"aliasIid = ${instance.iid}").foreach(av.delete[Profile])

    av.findChildLabel(instance.rootLabelIid, metaLabelName).foreach { metaLabel =>
      av.findChildLabel(metaLabel.iid, connectionsLabelName).foreach(av.delete[Label])
      av.findChildLabel(metaLabel.iid, verificationsLabelName).foreach(av.delete[Label])
      av.delete[Label](metaLabel)
    }

    instance
  }

  override protected def postDelete(instance: Alias): Alias = {
    val av = inject[AgentView]
    implicit val jdbcConn = inject[JdbcConn]

    av.delete[Label](av.fetch[Label](instance.rootLabelIid))

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
