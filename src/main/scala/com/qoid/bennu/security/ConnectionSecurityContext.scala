package com.qoid.bennu.security

import com.qoid.bennu.mapper.BennuMappedInstance
import com.qoid.bennu.mapper.BennuMapperCompanion
import com.qoid.bennu.model._
import com.qoid.bennu.model.id.AgentId
import com.qoid.bennu.model.id.InternalId
import com.qoid.bennu.query.ast.Query
import m3.Txn
import m3.jdbc._
import m3.predef._

object ConnectionSecurityContext {
  def apply[T](connectionIid: InternalId, injector: ScalaInjector)(fn: => T): T = {
    Txn {
      Txn.setViaTypename[SecurityContext](new ConnectionSecurityContext(connectionIid, injector))
      fn
    }
  }
}

class ConnectionSecurityContext(
  override val connectionIid: InternalId,
  injector: ScalaInjector
) extends SecurityContext {

  private lazy val agentAclMgr = injector.instance[AclManager].getAgentAclManager(agentId)
  private lazy val connection = SystemSecurityContext(Connection.fetch(connectionIid))

  override def agentId: AgentId = connection.agentId
  override def aliasIid: InternalId = connection.aliasIid

  override def constrictQuery(mapper: BennuMapperCompanion[_], query: Query): Query = {
    val query2 = mapper match {
      case Agent =>
        if (agentAclMgr.hasPermission(Agent, Permission.View)) {
          query
        } else {
          query.and(Query.parse("1 <> 1"))
        }
      case Alias => query.and(Query.parse(sql"iid in (${agentAclMgr.reachableAliasIids(Alias, Permission.View)})"))
      case Connection => query.and(Query.parse(sql"iid in (${agentAclMgr.reachableConnectionIids(Connection, Permission.View)})"))
      case Content => query.and(Query.parse(sql"iid in (${agentAclMgr.reachableContentIids(Content, Permission.View)})"))
      case Introduction =>
        query
          .and(Query.parse(sql"aConnectionIid in (${agentAclMgr.reachableConnectionIids(Introduction, Permission.View)})"))
          .and(Query.parse(sql"bConnectionIid in (${agentAclMgr.reachableConnectionIids(Introduction, Permission.View)})"))
      case Label => query.and(Query.parse(sql"iid in (${agentAclMgr.reachableLabelIids(Label, Permission.View)})"))
      case LabelAcl =>
        query
          .and(Query.parse(sql"connectionIid in (${agentAclMgr.reachableConnectionIids(LabelAcl, Permission.View)})"))
          .and(Query.parse(sql"labelIid in (${agentAclMgr.reachableLabelIids(LabelAcl, Permission.View)})"))
      case LabelChild =>
        query
          .and(Query.parse(sql"parentIid in (${agentAclMgr.reachableLabelIids(LabelChild, Permission.View)})"))
          .and(Query.parse(sql"childIid in (${agentAclMgr.reachableLabelIids(LabelChild, Permission.View)})"))
      case LabeledContent => query.and(Query.parse(sql"labelIid in (${agentAclMgr.reachableLabelIids(LabeledContent, Permission.View)})"))
      case Login => query.and(Query.parse(sql"aliasIid in (${agentAclMgr.reachableAliasIids(Login, Permission.View)})"))
      case Notification => query.and(Query.parse(sql"connectionIid in (${agentAclMgr.reachableConnectionIids(Notification, Permission.View)})"))
      case Profile => query.and(Query.parse(sql"aliasIid in (${agentAclMgr.reachableAliasIids(Profile, Permission.View)})"))
      case _ => query.and(Query.parse("1 <> 1"))
    }

    query2.and(Query.parse(sql"agentId = ${agentId}"))
  }

  override def canInsert[T <: BennuMappedInstance[T]](instance: T): Boolean = {
    instance match {
      case i: Agent => false
      case i: Alias => agentAclMgr.hasLabelIid(Alias, Permission.Insert, i.labelIid)
      case i: Connection => agentAclMgr.hasAliasIid(Connection, Permission.Insert, i.aliasIid)
      case i: Content => agentAclMgr.hasPermission(Content, Permission.Insert)
      case i: Introduction =>
        agentAclMgr.hasConnectionIid(Introduction, Permission.Insert, i.aConnectionIid) &&
          agentAclMgr.hasConnectionIid(Introduction, Permission.Insert, i.bConnectionIid)
      case i: Label => agentAclMgr.hasPermission(Label, Permission.Insert)
      case i: LabelAcl =>
        agentAclMgr.hasConnectionIid(LabelAcl, Permission.Insert, i.connectionIid) &&
          agentAclMgr.hasLabelIid(LabelAcl, Permission.Insert, i.labelIid)
      case i: LabelChild => agentAclMgr.hasLabelIid(LabelChild, Permission.Insert, i.parentIid)
      case i: LabeledContent => agentAclMgr.hasLabelIid(LabeledContent, Permission.Insert, i.labelIid)
      case i: Login => agentAclMgr.hasAliasIid(Login, Permission.Insert, i.aliasIid)
      case i: Notification => agentAclMgr.hasConnectionIid(Notification, Permission.Insert, i.connectionIid)
      case i: Profile => agentAclMgr.hasAliasIid(Profile, Permission.Insert, i.aliasIid)
      case _ => false
    }
  }

  override def canUpdate[T <: BennuMappedInstance[T]](instance: T): Boolean = {
    instance match {
      case i: Agent => agentAclMgr.hasPermission(Agent, Permission.Update)
      case i: Alias => agentAclMgr.hasAliasIid(Alias, Permission.Update, i.iid)
      case i: Connection => agentAclMgr.hasConnectionIid(Connection, Permission.Update, i.iid)
      case i: Content => agentAclMgr.hasContentIid(Content, Permission.Update, i.iid)
      case i: Introduction =>
        agentAclMgr.hasConnectionIid(Introduction, Permission.Update, i.aConnectionIid) &&
          agentAclMgr.hasConnectionIid(Introduction, Permission.Update, i.bConnectionIid)
      case i: Label => agentAclMgr.hasLabelIid(Label, Permission.Update, i.iid)
      case i: LabelAcl =>
        agentAclMgr.hasConnectionIid(LabelAcl, Permission.Update, i.connectionIid) &&
          agentAclMgr.hasLabelIid(LabelAcl, Permission.Update, i.labelIid)
      case i: LabelChild =>
        agentAclMgr.hasLabelIid(LabelChild, Permission.Update, i.parentIid) &&
          agentAclMgr.hasLabelIid(LabelChild, Permission.Update, i.childIid)
      case i: LabeledContent => agentAclMgr.hasLabelIid(LabeledContent, Permission.Update, i.labelIid)
      case i: Login => agentAclMgr.hasAliasIid(Login, Permission.Update, i.aliasIid)
      case i: Notification => agentAclMgr.hasConnectionIid(Notification, Permission.Update, i.connectionIid)
      case i: Profile => agentAclMgr.hasAliasIid(Profile, Permission.Update, i.aliasIid)
      case _ => false
    }
  }

  override def canDelete[T <: BennuMappedInstance[T]](instance: T): Boolean = {
    instance match {
      case i: Agent => false
      case i: Alias => agentAclMgr.hasAliasIid(Alias, Permission.Delete, i.iid)
      case i: Connection => agentAclMgr.hasConnectionIid(Connection, Permission.Delete, i.iid)
      case i: Content => agentAclMgr.hasContentIid(Content, Permission.Delete, i.iid)
      case i: Introduction =>
        agentAclMgr.hasConnectionIid(Introduction, Permission.Delete, i.aConnectionIid) &&
          agentAclMgr.hasConnectionIid(Introduction, Permission.Delete, i.bConnectionIid)
      case i: Label => agentAclMgr.hasLabelIid(Label, Permission.Delete, i.iid)
      case i: LabelAcl =>
        agentAclMgr.hasConnectionIid(LabelAcl, Permission.Delete, i.connectionIid) &&
          agentAclMgr.hasLabelIid(LabelAcl, Permission.Delete, i.labelIid)
      case i: LabelChild =>
        agentAclMgr.hasLabelIid(LabelChild, Permission.Delete, i.parentIid) &&
          agentAclMgr.hasLabelIid(LabelChild, Permission.Delete, i.childIid)
      case i: LabeledContent => agentAclMgr.hasLabelIid(LabeledContent, Permission.Delete, i.labelIid)
      case i: Login => agentAclMgr.hasAliasIid(Login, Permission.Delete, i.aliasIid)
      case i: Notification => agentAclMgr.hasConnectionIid(Notification, Permission.Delete, i.connectionIid)
      case i: Profile => agentAclMgr.hasAliasIid(Profile, Permission.Delete, i.aliasIid)
      case _ => false
    }
  }

  override def isAgentAdmin: Boolean = agentAclMgr.hasRole(Role.AgentAdmin)
}
