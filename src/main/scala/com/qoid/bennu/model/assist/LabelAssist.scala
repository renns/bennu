package com.qoid.bennu.model.assist

import java.sql.{Connection => JdbcConn}

import com.google.inject.Inject
import com.google.inject.Singleton
import com.qoid.bennu.JsonAssist._
import com.qoid.bennu.JsonAssist.jsondsl._
import com.qoid.bennu.model.Alias
import com.qoid.bennu.model.Connection
import com.qoid.bennu.model.Label
import com.qoid.bennu.model.LabelChild
import com.qoid.bennu.model.id.InternalId
import com.qoid.bennu.model.id.SemanticId
import com.qoid.bennu.security.AgentSecurityContext
import com.qoid.bennu.security.SecurityContext
import m3.jdbc._
import m3.predef._
import m3.predef.box._

@Singleton
class LabelAssist @Inject()(injector: ScalaInjector) {
  val metaLabelName = "Meta"
  val connectionsLabelName = "Connections"
  val connectionLabelName = "Connection"
  val verificationsLabelName = "Verifications"
  val auditLogLabelName = "Audit Log"

  val aliasLabelData: JValue = "color" -> "#000000"
  val metaLabelData: JValue = "color" -> "#7FBA00"

  def createMetaLabels(labelIid: InternalId): Label = {
    val metaLabel = Label.insert(Label(metaLabelName, None, data = metaLabelData))
    LabelChild.insert(LabelChild(labelIid, metaLabel.iid))

    val connectionsLabel = Label.insert(Label(connectionsLabelName, None, data = metaLabelData))
    LabelChild.insert(LabelChild(metaLabel.iid, connectionsLabel.iid))

    val verificationsLabel = Label.insert(Label(verificationsLabelName, None, data = metaLabelData))
    LabelChild.insert(LabelChild(metaLabel.iid, verificationsLabel.iid))

    val auditLogLabel = Label.insert(Label(auditLogLabelName, None, data = metaLabelData))
    LabelChild.insert(LabelChild(metaLabel.iid, auditLogLabel.iid))

    connectionsLabel
  }

  def findChildLabel(parentIid: InternalId, childName: String): Box[Label] = {
    val childIids = LabelChild.select(sql"parentIid = ${parentIid}").map(_.childIid).toList
    Label.selectBox(sql"name = ${childName} and iid in (${childIids})")
  }

  def resolveLabel(path: List[String]): Box[InternalId] = {

    def recurse(parentLabel: Label, path: List[String]): Box[InternalId] = {
      path match {
        case Nil => Full(parentLabel.iid)
        case hd :: tl => findChildLabel(parentLabel.iid, hd).flatMap(ch=>recurse(ch, tl))
      }
    }

    val securityContext = injector.instance[SecurityContext]

    AgentSecurityContext(securityContext.agentId) {
      val alias = Alias.fetch(securityContext.aliasIid)
      val label = Label.fetch(alias.labelIid)

      if (label.name == path.head) recurse(label, path.tail) ?~ s"label not found -- ${path.mkString("/")}"
      else recurse(label, path) ?~ s"label not found -- ${path.mkString("/")}"
    }
  }

  def resolveConnectionMetaLabel(): Box[InternalId] = {
    Full(Connection.fetch(injector.instance[SecurityContext].connectionIid).labelIid)
  }

  def resolveAuditLogMetaLabel(): Box[InternalId] = {
    resolveLabel(List(metaLabelName, auditLogLabelName))
  }

  def resolveLabelAncestry(parentLabelIid: InternalId): Iterator[InternalId] = {
    val jdbcConn = injector.instance[JdbcConn]

    // The following is executed outside of any security context
    jdbcConn.queryFor[InternalId](sql"""
      with recursive reachable_labels as (
        select iid as labelIid from label where iid = ${parentLabelIid}
        union all
        select lc.childIid as labelIid
        from labelchild lc
          join reachable_labels lt on lt.labelIid = lc.parentIid
      )
      select * from reachable_labels
    """)
  }

  def getSemanticLabels(semanticId: SemanticId): Iterator[InternalId] = {
    val securityContext = injector.instance[SecurityContext]

    AgentSecurityContext(securityContext.agentId) {
      Label.select(sql"semanticId = ${semanticId}").map(_.iid)
    }
  }
}
