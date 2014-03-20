package com.qoid.bennu.security

import com.qoid.bennu.JdbcAssist.BennuMapperCompanion
import com.qoid.bennu.model._
import com.qoid.bennu.model.id._
import com.qoid.bennu.squery.ast.Query
import java.sql.{ Connection => JdbcConn }
import m3.jdbc._
import m3.predef._
import m3.predef.box._

case class AgentSecurityContext(injector: ScalaInjector, agentId: AgentId) extends SecurityContext { sc =>

  private lazy val agent = Agent.fetch(agentId.asIid)(injector.instance[JdbcConn])
  private lazy val agentWhereClause: Query = Query.parse(sql"agentId = ${agentId}")

  override def aliasIid = agent.uberAliasIid

  override def createView = new AgentView {
    override def securityContext = sc
    override lazy val rootLabel = Label.fetch(rootAlias.rootLabelIid)(injector.instance[JdbcConn])
    private lazy val rootAlias = Alias.fetch(agent.uberAliasIid)(injector.instance[JdbcConn])

    override def validateInsertUpdateOrDelete[T <: HasInternalId](t: T): Box[T] = {
      if ( agentId == t.agentId ) Full(t)
      else Failure(s"agent id of the object being validated ${t.agentId} is not the same as current agent in the security context")
    }

    override def constrict[T <: HasInternalId](mapper: BennuMapperCompanion[T], query: Query): Query = query.and(agentWhereClause.expr)

    override def hasAccessToAlias(aliasIid: InternalId): Boolean = {
      select[Alias](sql"iid = $aliasIid").nonEmpty
    }
  }
}
