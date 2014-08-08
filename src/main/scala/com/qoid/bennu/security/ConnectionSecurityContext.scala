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
        if (agentAclMgr.hasPermission(connectionIid, Agent, Permission.View)) query
        else query.and(Query.parse("1 <> 1"))
      case Alias =>
        query.and(Query.parse(sql"iid in (${agentAclMgr.reachableAliasIids(connectionIid, Alias, Permission.View)})"))
      case Connection =>
        query.and(Query.parse(sql"iid in (${agentAclMgr.reachableConnectionIids(connectionIid, Connection, Permission.View)})"))
      case Content =>
        query.and(Query.parse(sql"iid in (${agentAclMgr.reachableContentIids(connectionIid, Content, Permission.View)})"))
      case Introduction =>
        query
          .and(Query.parse(sql"aConnectionIid in (${agentAclMgr.reachableConnectionIids(connectionIid, Introduction, Permission.View)})"))
          .and(Query.parse(sql"bConnectionIid in (${agentAclMgr.reachableConnectionIids(connectionIid, Introduction, Permission.View)})"))
      case Label =>
        query.and(Query.parse(sql"iid in (${agentAclMgr.reachableLabelIids(connectionIid, Label, Permission.View)})"))
      case LabelAcl =>
        query
          .and(Query.parse(sql"connectionIid in (${agentAclMgr.reachableConnectionIids(connectionIid, LabelAcl, Permission.View)})"))
          .and(Query.parse(sql"labelIid in (${agentAclMgr.reachableLabelIids(connectionIid, LabelAcl, Permission.View)})"))
      case LabelChild =>
        query
          .and(Query.parse(sql"parentIid in (${agentAclMgr.reachableLabelIids(connectionIid, LabelChild, Permission.View)})"))
          .and(Query.parse(sql"childIid in (${agentAclMgr.reachableLabelIids(connectionIid, LabelChild, Permission.View)})"))
      case LabeledContent =>
        query.and(Query.parse(sql"labelIid in (${agentAclMgr.reachableLabelIids(connectionIid, LabeledContent, Permission.View)})"))
      case Login =>
        query.and(Query.parse(sql"aliasIid in (${agentAclMgr.reachableAliasIids(connectionIid, Login, Permission.View)})"))
      case Notification =>
        query.and(Query.parse(sql"connectionIid in (${agentAclMgr.reachableConnectionIids(connectionIid, Notification, Permission.View)})"))
      case Profile =>
        query.and(Query.parse(sql"aliasIid in (${agentAclMgr.reachableAliasIids(connectionIid, Profile, Permission.View)})"))
      case _ =>
        query.and(Query.parse("1 <> 1"))
    }

    query2.and(Query.parse(sql"agentId = ${agentId}"))
  }

  override def canInsert[T <: BennuMappedInstance[T]](instance: T): Boolean = {
    instance match {
      case i: Agent => false
      case i: Alias => agentAclMgr.hasLabelIid(connectionIid, Alias, Permission.Insert, i.labelIid)
      case i: Connection => agentAclMgr.hasAliasIid(connectionIid, Connection, Permission.Insert, i.aliasIid)
      case i: Content => agentAclMgr.hasPermission(connectionIid, Content, Permission.Insert)
      case i: Introduction =>
        agentAclMgr.hasConnectionIid(connectionIid, Introduction, Permission.Insert, i.aConnectionIid) &&
          agentAclMgr.hasConnectionIid(connectionIid, Introduction, Permission.Insert, i.bConnectionIid)
      case i: Label => agentAclMgr.hasPermission(connectionIid, Label, Permission.Insert)
      case i: LabelAcl =>
        agentAclMgr.hasConnectionIid(connectionIid, LabelAcl, Permission.Insert, i.connectionIid) &&
          agentAclMgr.hasLabelIid(connectionIid, LabelAcl, Permission.Insert, i.labelIid)
        //TODO: must be able to maintain passed in role
      case i: LabelChild => agentAclMgr.hasLabelIid(connectionIid, LabelChild, Permission.Insert, i.parentIid)
      case i: LabeledContent => agentAclMgr.hasLabelIid(connectionIid, LabeledContent, Permission.Insert, i.labelIid)
      case i: Login => agentAclMgr.hasAliasIid(connectionIid, Login, Permission.Insert, i.aliasIid)
      case i: Notification => agentAclMgr.hasConnectionIid(connectionIid, Notification, Permission.Insert, i.connectionIid)
      case i: Profile => agentAclMgr.hasAliasIid(connectionIid, Profile, Permission.Insert, i.aliasIid)
      case _ => false
    }
  }

  override def canUpdate[T <: BennuMappedInstance[T]](instance: T): Boolean = {
    instance match {
      case i: Agent => agentAclMgr.hasPermission(connectionIid, Agent, Permission.Update)
      case i: Alias => agentAclMgr.hasAliasIid(connectionIid, Alias, Permission.Update, i.iid)
      case i: Connection => agentAclMgr.hasConnectionIid(connectionIid, Connection, Permission.Update, i.iid)
      case i: Content => agentAclMgr.hasContentIid(connectionIid, Content, Permission.Update, i.iid)
      case i: Introduction =>
        agentAclMgr.hasConnectionIid(connectionIid, Introduction, Permission.Update, i.aConnectionIid) &&
          agentAclMgr.hasConnectionIid(connectionIid, Introduction, Permission.Update, i.bConnectionIid)
      case i: Label => agentAclMgr.hasLabelIid(connectionIid, Label, Permission.Update, i.iid)
      case i: LabelAcl =>
        agentAclMgr.hasConnectionIid(connectionIid, LabelAcl, Permission.Update, i.connectionIid) &&
          agentAclMgr.hasLabelIid(connectionIid, LabelAcl, Permission.Update, i.labelIid)
        //TODO: must be able to maintain passed in role
      case i: LabelChild =>
        agentAclMgr.hasLabelIid(connectionIid, LabelChild, Permission.Update, i.parentIid) &&
          agentAclMgr.hasLabelIid(connectionIid, LabelChild, Permission.Update, i.childIid)
      case i: LabeledContent => agentAclMgr.hasLabelIid(connectionIid, LabeledContent, Permission.Update, i.labelIid)
      case i: Login => agentAclMgr.hasAliasIid(connectionIid, Login, Permission.Update, i.aliasIid)
      case i: Notification => agentAclMgr.hasConnectionIid(connectionIid, Notification, Permission.Update, i.connectionIid)
      case i: Profile => agentAclMgr.hasAliasIid(connectionIid, Profile, Permission.Update, i.aliasIid)
      case _ => false
    }
  }

  override def canDelete[T <: BennuMappedInstance[T]](instance: T): Boolean = {
    instance match {
      case i: Agent => false
      case i: Alias => agentAclMgr.hasAliasIid(connectionIid, Alias, Permission.Delete, i.iid)
      case i: Connection => agentAclMgr.hasConnectionIid(connectionIid, Connection, Permission.Delete, i.iid)
      case i: Content => agentAclMgr.hasContentIid(connectionIid, Content, Permission.Delete, i.iid)
      case i: Introduction =>
        agentAclMgr.hasConnectionIid(connectionIid, Introduction, Permission.Delete, i.aConnectionIid) &&
          agentAclMgr.hasConnectionIid(connectionIid, Introduction, Permission.Delete, i.bConnectionIid)
      case i: Label => agentAclMgr.hasLabelIid(connectionIid, Label, Permission.Delete, i.iid)
      case i: LabelAcl =>
        agentAclMgr.hasConnectionIid(connectionIid, LabelAcl, Permission.Delete, i.connectionIid) &&
          agentAclMgr.hasLabelIid(connectionIid, LabelAcl, Permission.Delete, i.labelIid)
        //TODO: must be able to maintain passed in role
      case i: LabelChild =>
        agentAclMgr.hasLabelIid(connectionIid, LabelChild, Permission.Delete, i.parentIid) &&
          agentAclMgr.hasLabelIid(connectionIid, LabelChild, Permission.Delete, i.childIid)
      case i: LabeledContent => agentAclMgr.hasLabelIid(connectionIid, LabeledContent, Permission.Delete, i.labelIid)
      case i: Login => agentAclMgr.hasAliasIid(connectionIid, Login, Permission.Delete, i.aliasIid)
      case i: Notification => agentAclMgr.hasConnectionIid(connectionIid, Notification, Permission.Delete, i.connectionIid)
      case i: Profile => agentAclMgr.hasAliasIid(connectionIid, Profile, Permission.Delete, i.aliasIid)
      case _ => false
    }
  }

  override def canExportAgent: Boolean = agentAclMgr.getAcls(connectionIid).exists(_.labelAcl.role.canExportAgent)
  override def canSpawnSession: Boolean = agentAclMgr.getAcls(connectionIid).exists(_.labelAcl.role.canSpawnSession)
}
