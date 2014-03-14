package com.qoid.bennu.security

import com.qoid.bennu.JdbcAssist.BennuMapperCompanion
import com.qoid.bennu.model._
import com.qoid.bennu.squery.ast.Query
import java.sql.{ Connection => JdbcConn }
import m3.predef.box._
import net.codingwell.scalaguice.InjectorExtensions.ScalaInjector

case class AgentSecurityContext(injector: ScalaInjector, agentId: AgentId) extends SecurityContext { sc =>

  lazy val agent = Agent.fetch(agentId.asIid)(injector.instance[JdbcConn])

  override def aliasIid = agent.uberAliasIid

  override def createView = new AgentView {
    override def securityContext = sc
    override lazy val rootLabel = Label.fetch(rootAlias.rootLabelIid)(injector.instance[JdbcConn])
    lazy val rootAlias = Alias.fetch(agent.uberAliasIid)(injector.instance[JdbcConn])

    override def validateInsertUpdateOrDelete[T <: HasInternalId](t: T): Box[T] = {
      if ( agentId == t.agentId ) Full(t)
      else Failure(s"agent id of the object being validated ${t.agentId} is not the same as current agent in the security context")
    }

    override def readResolve[T <: HasInternalId](t: T): Box[T] = {
      if ( agentId == t.agentId ) Full(t)
      else Failure("agent cannot read another agents data")
    }

    override def constrict[T <: HasInternalId](mapper: BennuMapperCompanion[T], query: Query): Query = query.and(agentWhereClause.expr)
  }
}
