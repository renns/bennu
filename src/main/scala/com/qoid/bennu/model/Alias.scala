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
import net.model3.chrono.DateTime

object Alias extends BennuMapperCompanion[Alias] {
  private val aliasLabelColor = "#000000"
  private val metaLabelColor = "#7FBA00"

  val metaLabelName = "Meta"
  val verificationsLabelName = "Verifications"
  val connectionsLabelName = "Connections"

  override protected def preInsert(instance: Alias): Alias = {
    val av = inject[AgentView]

    val connectionIid = instance.connectionIid match {
      case InternalId("") => InternalId.random
      case _ => instance.connectionIid
    }

    val peerId = PeerId.random
    val connection = av.insert[Connection](Connection(instance.iid, peerId, peerId, iid = connectionIid))

    val label = av.insert[Label](Label(instance.name, data = "color" -> aliasLabelColor))

    instance.copy(rootLabelIid = label.iid, connectionIid = connection.iid)
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

    // Create connection to security context's alias
    if (av.securityContext.aliasIid != instance.iid) {
      Txn {
        val peerId1 = PeerId.random
        val peerId2 = PeerId.random
        av.insert[Connection](Connection(instance.iid, peerId1, peerId2))
        av.insert[Connection](Connection(av.securityContext.aliasIid, peerId2, peerId1))
      }
    }

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
  connectionIid: InternalId = InternalId(""),
  created: DateTime = new DateTime,
  modified: DateTime = new DateTime,
  createdByConnectionIid: InternalId = InternalId(""),
  modifiedByConnectionIid: InternalId = InternalId("")
) extends HasInternalId with BennuMappedInstance[Alias] { self =>
  
  type TInstance = Alias
  
  def mapper = Alias

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
