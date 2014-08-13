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
  def apply[T](connectionIid: InternalId, degreesOfVisibility: Int, injector: ScalaInjector)(fn: => T): T = {
    Txn {
      Txn.setViaTypename[SecurityContext](new ConnectionSecurityContext(connectionIid, degreesOfVisibility, injector))
      fn
    }
  }
}

class ConnectionSecurityContext(
  override val connectionIid: InternalId,
  degreesOfVisibility: Int,
  injector: ScalaInjector
) extends SecurityContext {

  //TODO: check degreesOfVisibility in all permissions

  private lazy val agentAclMgr = injector.instance[AclManager].getAgentAclManager(agentId)
  private lazy val connection = SystemSecurityContext(Connection.fetch(connectionIid))

  override def agentId: AgentId = connection.agentId
  override def aliasIid: InternalId = connection.aliasIid

  override def constrictQuery(mapper: BennuMapperCompanion[_], query: Query): Query = {
    val query2 = mapper match {
      case Agent =>
        if (agentAclMgr.hasPermission(connectionIid, Agent, Permission.View, degreesOfVisibility)) query
        else query.and(Query.parse("1 <> 1"))
      case Alias =>
        query.and(Query.parse(sql"iid in (${agentAclMgr.reachableAliasIids(connectionIid, Alias, Permission.View, degreesOfVisibility)})"))
      case Connection =>
        query.and(Query.parse(sql"iid in (${agentAclMgr.reachableConnectionIids(connectionIid, Connection, Permission.View, degreesOfVisibility)})"))
      case Content =>
        query.and(Query.parse(sql"iid in (${agentAclMgr.reachableContentIids(connectionIid, Content, Permission.View, degreesOfVisibility)})"))
      case Introduction =>
        query
          .and(Query.parse(sql"aConnectionIid in (${agentAclMgr.reachableConnectionIids(connectionIid, Introduction, Permission.View, degreesOfVisibility)})"))
          .and(Query.parse(sql"bConnectionIid in (${agentAclMgr.reachableConnectionIids(connectionIid, Introduction, Permission.View, degreesOfVisibility)})"))
      case Label =>
        query.and(Query.parse(sql"iid in (${agentAclMgr.reachableLabelIids(connectionIid, Label, Permission.View, degreesOfVisibility)})"))
      case LabelAcl =>
        query
          .and(Query.parse(sql"connectionIid in (${agentAclMgr.reachableConnectionIids(connectionIid, LabelAcl, Permission.View, degreesOfVisibility)})"))
          .and(Query.parse(sql"labelIid in (${agentAclMgr.reachableLabelIids(connectionIid, LabelAcl, Permission.View, degreesOfVisibility)})"))
      case LabelChild =>
        query
          .and(Query.parse(sql"parentIid in (${agentAclMgr.reachableLabelIids(connectionIid, LabelChild, Permission.View, degreesOfVisibility)})"))
          .and(Query.parse(sql"childIid in (${agentAclMgr.reachableLabelIids(connectionIid, LabelChild, Permission.View, degreesOfVisibility)})"))
      case LabeledContent =>
        query.and(Query.parse(sql"labelIid in (${agentAclMgr.reachableLabelIids(connectionIid, LabeledContent, Permission.View, degreesOfVisibility)})"))
      case Login =>
        query.and(Query.parse(sql"aliasIid in (${agentAclMgr.reachableAliasIids(connectionIid, Login, Permission.View, degreesOfVisibility)})"))
      case Notification =>
        query.and(Query.parse(sql"createdByConnectionIid in (${agentAclMgr.reachableConnectionIids(connectionIid, Notification, Permission.View, degreesOfVisibility)})"))
      case Profile =>
        query.and(Query.parse(sql"aliasIid in (${agentAclMgr.reachableAliasIids(connectionIid, Profile, Permission.View, degreesOfVisibility)})"))
      case _ =>
        query.and(Query.parse("1 <> 1"))
    }

    query2.and(Query.parse(sql"agentId = ${agentId}"))
  }

  override def canInsert[T <: BennuMappedInstance[T]](instance: T): Boolean = {
    instance match {
      case i: Agent => false
      case i: Alias => agentAclMgr.hasLabelIid(connectionIid, Alias, Permission.Insert, degreesOfVisibility, i.labelIid)
      case i: Connection => agentAclMgr.hasAliasIid(connectionIid, Connection, Permission.Insert, degreesOfVisibility, i.aliasIid)
      case i: Content => agentAclMgr.hasPermission(connectionIid, Content, Permission.Insert, degreesOfVisibility)
      case i: Introduction =>
        agentAclMgr.hasConnectionIid(connectionIid, Introduction, Permission.Insert, degreesOfVisibility, i.aConnectionIid) &&
          agentAclMgr.hasConnectionIid(connectionIid, Introduction, Permission.Insert, degreesOfVisibility, i.bConnectionIid)
      case i: Label => agentAclMgr.hasPermission(connectionIid, Label, Permission.Insert, degreesOfVisibility)
      case i: LabelAcl =>
        agentAclMgr.hasConnectionIid(connectionIid, LabelAcl, Permission.Insert, degreesOfVisibility, i.connectionIid) &&
          agentAclMgr.hasLabelIid(connectionIid, LabelAcl, Permission.Insert, degreesOfVisibility, i.labelIid)
        //TODO: must be able to maintain passed in role
      case i: LabelChild => agentAclMgr.hasLabelIid(connectionIid, LabelChild, Permission.Insert, degreesOfVisibility, i.parentIid)
      case i: LabeledContent => agentAclMgr.hasLabelIid(connectionIid, LabeledContent, Permission.Insert, degreesOfVisibility, i.labelIid)
      case i: Login => agentAclMgr.hasAliasIid(connectionIid, Login, Permission.Insert, degreesOfVisibility, i.aliasIid)
      case i: Notification => true
      case i: Profile => agentAclMgr.hasAliasIid(connectionIid, Profile, Permission.Insert, degreesOfVisibility, i.aliasIid)
      case _ => false
    }
  }

  override def canUpdate[T <: BennuMappedInstance[T]](instance: T): Boolean = {
    instance match {
      case i: Agent => agentAclMgr.hasPermission(connectionIid, Agent, Permission.Update, degreesOfVisibility)
      case i: Alias => agentAclMgr.hasAliasIid(connectionIid, Alias, Permission.Update, degreesOfVisibility, i.iid)
      case i: Connection => agentAclMgr.hasConnectionIid(connectionIid, Connection, Permission.Update, degreesOfVisibility, i.iid)
      case i: Content => agentAclMgr.hasContentIid(connectionIid, Content, Permission.Update, degreesOfVisibility, i.iid)
      case i: Introduction =>
        agentAclMgr.hasConnectionIid(connectionIid, Introduction, Permission.Update, degreesOfVisibility, i.aConnectionIid) &&
          agentAclMgr.hasConnectionIid(connectionIid, Introduction, Permission.Update, degreesOfVisibility, i.bConnectionIid)
      case i: Label => agentAclMgr.hasLabelIid(connectionIid, Label, Permission.Update, degreesOfVisibility, i.iid)
      case i: LabelAcl =>
        agentAclMgr.hasConnectionIid(connectionIid, LabelAcl, Permission.Update, degreesOfVisibility, i.connectionIid) &&
          agentAclMgr.hasLabelIid(connectionIid, LabelAcl, Permission.Update, degreesOfVisibility, i.labelIid)
        //TODO: must be able to maintain passed in role
      case i: LabelChild =>
        agentAclMgr.hasLabelIid(connectionIid, LabelChild, Permission.Update, degreesOfVisibility, i.parentIid) &&
          agentAclMgr.hasLabelIid(connectionIid, LabelChild, Permission.Update, degreesOfVisibility, i.childIid)
      case i: LabeledContent => agentAclMgr.hasLabelIid(connectionIid, LabeledContent, Permission.Update, degreesOfVisibility, i.labelIid)
      case i: Login => agentAclMgr.hasAliasIid(connectionIid, Login, Permission.Update, degreesOfVisibility, i.aliasIid)
      case i: Notification => agentAclMgr.hasConnectionIid(connectionIid, Notification, Permission.Update, degreesOfVisibility, i.createdByConnectionIid)
      case i: Profile => agentAclMgr.hasAliasIid(connectionIid, Profile, Permission.Update, degreesOfVisibility, i.aliasIid)
      case _ => false
    }
  }

  override def canDelete[T <: BennuMappedInstance[T]](instance: T): Boolean = {
    instance match {
      case i: Agent => false
      case i: Alias => agentAclMgr.hasAliasIid(connectionIid, Alias, Permission.Delete, degreesOfVisibility, i.iid)
      case i: Connection => agentAclMgr.hasConnectionIid(connectionIid, Connection, Permission.Delete, degreesOfVisibility, i.iid)
      case i: Content => agentAclMgr.hasContentIid(connectionIid, Content, Permission.Delete, degreesOfVisibility, i.iid)
      case i: Introduction =>
        agentAclMgr.hasConnectionIid(connectionIid, Introduction, Permission.Delete, degreesOfVisibility, i.aConnectionIid) &&
          agentAclMgr.hasConnectionIid(connectionIid, Introduction, Permission.Delete, degreesOfVisibility, i.bConnectionIid)
      case i: Label => agentAclMgr.hasLabelIid(connectionIid, Label, Permission.Delete, degreesOfVisibility, i.iid)
      case i: LabelAcl =>
        agentAclMgr.hasConnectionIid(connectionIid, LabelAcl, Permission.Delete, degreesOfVisibility, i.connectionIid) &&
          agentAclMgr.hasLabelIid(connectionIid, LabelAcl, Permission.Delete, degreesOfVisibility, i.labelIid)
        //TODO: must be able to maintain passed in role
      case i: LabelChild =>
        agentAclMgr.hasLabelIid(connectionIid, LabelChild, Permission.Delete, degreesOfVisibility, i.parentIid) &&
          agentAclMgr.hasLabelIid(connectionIid, LabelChild, Permission.Delete, degreesOfVisibility, i.childIid)
      case i: LabeledContent => agentAclMgr.hasLabelIid(connectionIid, LabeledContent, Permission.Delete, degreesOfVisibility, i.labelIid)
      case i: Login => agentAclMgr.hasAliasIid(connectionIid, Login, Permission.Delete, degreesOfVisibility, i.aliasIid)
      case i: Notification => agentAclMgr.hasConnectionIid(connectionIid, Notification, Permission.Delete, degreesOfVisibility, i.createdByConnectionIid)
      case i: Profile => agentAclMgr.hasAliasIid(connectionIid, Profile, Permission.Delete, degreesOfVisibility, i.aliasIid)
      case _ => false
    }
  }

  override def canExportAgent: Boolean = agentAclMgr.getAcls(connectionIid).exists(_.labelAcl.role.canExportAgent)
  override def canSpawnSession: Boolean = agentAclMgr.getAcls(connectionIid).exists(_.labelAcl.role.canSpawnSession)
}
