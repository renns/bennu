package com.qoid.bennu.security

import com.qoid.bennu.JdbcAssist.BennuMapperCompanion
import com.qoid.bennu.model._
import com.qoid.bennu.squery.ast.Query
import java.sql.{ Connection => JdbcConn }
import m3.jdbc._
import m3.predef.box._
import net.codingwell.scalaguice.InjectorExtensions.ScalaInjector

case class AliasSecurityContext(injector: ScalaInjector, aliasIid: InternalId) extends SecurityContext { sc =>

  override lazy val agentId = Alias.fetch(aliasIid)(injector.instance[JdbcConn]).agentId

  override def createView = new AgentView {
    override def securityContext = sc
    override lazy val rootLabel = Label.fetch(alias.rootLabelIid)(injector.instance[JdbcConn])

    override def validateInsertUpdateOrDelete[T <: HasInternalId](t: T): Box[T] = {
      if ( agentId == t.agentId ) Full(t)
      else Failure(s"agent id of the object being validated ${t.agentId} is not the same as current agent in the security context")
    }

    lazy val alias = Alias.fetch(aliasIid)(injector.instance[JdbcConn])

    lazy val labelTreeLabelIids = injector.instance[JdbcConn].queryFor[InternalId](sql"""
  with recursive reachable_labels as (
     select rootLabelIid as labelIid from alias where iid = ${aliasIid} and deleted = false
     union all
     -- the following is the recursion query
     select lc.childIid as labelIid
     from labelchild lc
       join reachable_labels lt on lt.labelIid = lc.parentIid
     where lc.deleted = false
  )
  select *
  from reachable_labels
      """).toList

    lazy val connectionLabelIids = injector.instance[JdbcConn].queryFor[InternalId](sql"""
          select
            metaLabelIid
          from
            connection
          where
            aliasIid in (${reachableAliasIids}) and deleted = false
      """).toList

    lazy val reachableAliasIids = injector.instance[JdbcConn].queryFor[InternalId](sql"""
          select
            iid
          from
            alias
          where
            rootLabelIid in (${labelTreeLabelIids}) and deleted = false
      """).toList

    lazy val reachableConnectionIids = injector.instance[JdbcConn].queryFor[InternalId](sql"""
          select
            iid
          from
            connection
          where
            aliasIid in (${reachableAliasIids}) and deleted = false
      """).toList

    lazy val reachableLabelIids = labelTreeLabelIids ::: connectionLabelIids

    override def readResolve[T <: HasInternalId](t: T): Box[T] = {
      if ( agentId == t.agentId ) Full(t)
      else Failure("agent cannot read another agents data")
    }

    override def constrict[T <: HasInternalId](mapper: BennuMapperCompanion[T], query: Query): Query = {
      mapper.typeName.toLowerCase match {
        case "connection" => query.and(Query.parse(sql"""aliasIid in (${reachableAliasIids})"""))
        case "agent" => query.and(Query.parse(sql"""agentId = ${agentId}"""))
        case "alias" => query.and(Query.parse(sql"""iid in (${reachableAliasIids})"""))
        case "labelacl" => query.and(Query.parse(sql"""labelIid in (${reachableLabelIids})"""))
        case "label" => query.and(Query.parse(sql"""iid in (${reachableLabelIids})"""))
        case "content" => {
          // TODO this needs to be optimized
          val content = injector.instance[JdbcConn].queryFor[InternalId](sql"""select contentIid from labeledcontent where labelIid in (${reachableLabelIids}) and deleted = false""").toList
          query.and(Query.parse(sql"""iid in (${content})"""))
        }
        case "introduction" => query.and(Query.parse(sql"""aConnectionIid in (${reachableConnectionIids}) and bConnectionIid in (${reachableConnectionIids})"""))
        case "labelchild" => query.and(Query.parse(sql"""parentIid in (${reachableLabelIids}) and childIid in (${reachableLabelIids})"""))
        case "labeledcontent" => query.and(Query.parse(sql"""labelIid in (${reachableLabelIids})"""))
        case "notification" => query.and(Query.parse(sql"""fromConnectionIid in (${reachableConnectionIids})"""))
        case _ => query.and(Query.parse(sql"""1 <> 1"""))
      }
    }
  }
}