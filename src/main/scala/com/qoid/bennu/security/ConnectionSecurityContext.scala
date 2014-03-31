package com.qoid.bennu.security

import com.qoid.bennu.JdbcAssist.BennuMapperCompanion
import com.qoid.bennu.model._
import com.qoid.bennu.model.id.InternalId
import com.qoid.bennu.squery.ast.Query
import java.sql.{ Connection => JdbcConn }
import m3.jdbc._
import m3.predef._
import m3.predef.box._

case class ConnectionSecurityContext(injector: ScalaInjector, connectionIid: InternalId) extends SecurityContext { sc =>

  override lazy val agentId = Connection.fetch(connectionIid)(injector.instance[JdbcConn]).agentId
  override lazy val aliasIid = Connection.fetch(connectionIid)(injector.instance[JdbcConn]).aliasIid

  override def createView = new AgentView {
    override def securityContext = sc
    override lazy val rootLabel = Label.fetch(alias.rootLabelIid)(injector.instance[JdbcConn])
    private lazy val connection = Connection.fetch(connectionIid)(injector.instance[JdbcConn])
    private lazy val alias = Alias.fetch(connection.aliasIid)(injector.instance[JdbcConn])
    private lazy val metaLabel = rootLabel.findChild(Alias.metaLabelName)(injector.instance[JdbcConn]).head
    private lazy val verificationsMetaLabel = metaLabel.findChild(Alias.verificationsLabelName)(injector.instance[JdbcConn]).head

    override def validateInsertUpdateOrDelete[T <: HasInternalId](t: T): Box[T] = Failure("connections cannot do insert, update or delete actions on other agents")

    private lazy val reachableLabels: List[InternalId] = connection.metaLabelIid :: verificationsMetaLabel.iid :: injector.instance[JdbcConn].queryFor[InternalId](sql"""
  with recursive reachable_labels as (
     select labelIid
     from labelacl
     where connectionIid = ${connectionIid} and deleted = false  -- this is the starting point query
     union all
     -- the following is the recursion query
     select lc.childIid
     from labelchild lc
       join reachable_labels lt on lt.labelIid = lc.parentIid
     where lc.deleted = false
  )
  select *
  from reachable_labels
      """).toList

    private lazy val reachableLabelAcls = injector.instance[JdbcConn].queryFor[InternalId](sql"""
        select iid from labelacl where connectionIid = ${connectionIid} and deleted = false
      """).toList

    override def constrict[T <: HasInternalId](mapper: BennuMapperCompanion[T], query: Query): Query = {
      mapper.typeName.toLowerCase match {
        case "labelacl" => query.and(Query.parse(sql"""iid in (${reachableLabelAcls})"""))
        case "label" => query.and(Query.parse(sql"""iid in (${reachableLabels})"""))
        case "content" =>
          // TODO this needs to be optimized
          val content = injector.instance[JdbcConn].queryFor[InternalId](sql"""select contentIid from labeledcontent where labelIid in (${reachableLabels}) and deleted = false""").toList
          query.and(Query.parse(sql"""iid in (${content})"""))
        case "labelchild" => query.and(Query.parse(sql"""parentIid in (${reachableLabels}) and childIid in (${reachableLabels})"""))
        case "labeledcontent" => query.and(Query.parse(sql"""labelIid in (${reachableLabels})"""))
        case "profile" => query.and(Query.parse(sql"""aliasIid = ${aliasIid.value}"""))
        case _ => query.and(Query.parse(sql"""1 <> 1"""))
      }
    }

    override def resolveConnectionMetaLabel(): Box[InternalId] = Full(connection.metaLabelIid)
  }
}
