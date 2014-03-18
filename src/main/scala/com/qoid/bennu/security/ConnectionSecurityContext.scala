package com.qoid.bennu.security

import com.qoid.bennu.ErrorCode
import com.qoid.bennu.JdbcAssist.BennuMapperCompanion
import com.qoid.bennu.ServiceException
import com.qoid.bennu.model._
import com.qoid.bennu.squery.ast.Query
import java.sql.{ Connection => JdbcConn }
import m3.jdbc._
import m3.predef.box._
import net.codingwell.scalaguice.InjectorExtensions.ScalaInjector

object ConnectionSecurityContext {
  val readableTypes: Set[Mapper[_ <: HasInternalId, InternalId]] = Set[Mapper[_ <: HasInternalId, InternalId]](
    Content,
    Label,
    LabelAcl,
    LabelChild,
    LabeledContent
  )
}

case class ConnectionSecurityContext(injector: ScalaInjector, connectionIid: InternalId) extends SecurityContext { sc =>

  override lazy val agentId = Connection.fetch(connectionIid)(injector.instance[JdbcConn]).agentId
  override lazy val aliasIid = Connection.fetch(connectionIid)(injector.instance[JdbcConn]).aliasIid

  override def createView = new AgentView {
    override def securityContext = sc
    override lazy val rootLabel = Label.fetch(alias.rootLabelIid)(injector.instance[JdbcConn])
    lazy val connection = Connection.fetch(connectionIid)(injector.instance[JdbcConn])
    lazy val alias = Alias.fetch(connection.aliasIid)(injector.instance[JdbcConn])

    override def validateInsertUpdateOrDelete[T <: HasInternalId](t: T): Box[T] = Failure("connections cannot do insert, update or delete actions on other agents")

    lazy val reachableLabels: List[InternalId] = connection.metaLabelIid :: injector.instance[JdbcConn].queryFor[InternalId](sql"""
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

    lazy val reachableLabelAcls = injector.instance[JdbcConn].queryFor[InternalId](sql"""
        select iid from labelacl where connectionIid = ${connectionIid} and deleted = false
      """).toList

    override def readResolve[T <: HasInternalId](t: T): Box[T] = {
      if ( agentId != t.agentId ) Failure("agent cannot read another agents data")
      else if ( ConnectionSecurityContext.readableTypes.contains(t.mapper.asInstanceOf[Mapper[_ <: HasInternalId, InternalId]]) ) Full(t)
      else Empty
    }

    override def constrict[T <: HasInternalId](mapper: BennuMapperCompanion[T], query: Query): Query = {
      if ( ConnectionSecurityContext.readableTypes.contains(mapper) ) {
        mapper.typeName.toLowerCase match {
          case "labelacl" => query.and(Query.parse(sql"""iid in (${reachableLabelAcls})"""))
          case "label" => query.and(Query.parse(sql"""iid in (${reachableLabels})"""))
          case "content" =>
            // TODO this needs to be optimized
            val content = injector.instance[JdbcConn].queryFor[InternalId](sql"""select contentIid from labeledcontent where labelIid in (${reachableLabels}) and deleted = false""").toList
            query.and(Query.parse(sql"""iid in (${content})"""))
          case "labelchild" => query.and(Query.parse(sql"""parentIid in (${reachableLabels}) and childIid in (${reachableLabels})"""))
          case "labeledcontent" => query.and(Query.parse(sql"""labelIid in (${reachableLabels})"""))
        }
      } else {
        throw new ServiceException(s"connection is not allowed to access ${mapper.typeName}", ErrorCode.Forbidden)
      }
    }

    override def resolveConnectionMetaLabel(): Box[InternalId] = Full(connection.metaLabelIid)
  }
}
