package com.qoid.bennu.security

import com.qoid.bennu.mapper.BennuMapperCompanion
import com.qoid.bennu.model.LabelAcl
import com.qoid.bennu.model.id.AgentId
import com.qoid.bennu.model.id.InternalId
import m3.predef._

class AgentAclManager(agentId: AgentId, injector: ScalaInjector) {
  private var aclsLoaded: Boolean = false

  private var _acls = List.empty[Acl]

  def reachableLabelIids(permissionType: BennuMapperCompanion[_], permission: Permission): List[InternalId] = {
    getAcls.filter(_.role.hasPermission(permissionType, permission)).flatMap(_.reachableLabelIids)
  }

  def reachableAliasIids(permissionType: BennuMapperCompanion[_], permission: Permission): List[InternalId] = {
    getAcls.filter(_.role.hasPermission(permissionType, permission)).flatMap(_.reachableAliasIids)
  }

  def reachableConnectionIids(permissionType: BennuMapperCompanion[_], permission: Permission): List[InternalId] = {
    getAcls.filter(_.role.hasPermission(permissionType, permission)).flatMap(_.reachableConnectionIids)
  }

  def reachableContentIids(permissionType: BennuMapperCompanion[_], permission: Permission): List[InternalId] = {
    getAcls.filter(_.role.hasPermission(permissionType, permission)).flatMap(_.reachableContentIids)
  }

  def hasLabelIid(permissionType: BennuMapperCompanion[_], permission: Permission, iid: InternalId): Boolean = {
    reachableLabelIids(permissionType, permission).contains(iid)
  }

  def hasAliasIid(permissionType: BennuMapperCompanion[_], permission: Permission, iid: InternalId): Boolean = {
    reachableAliasIids(permissionType, permission).contains(iid)
  }

  def hasConnectionIid(permissionType: BennuMapperCompanion[_], permission: Permission, iid: InternalId): Boolean = {
    reachableConnectionIids(permissionType, permission).contains(iid)
  }

  def hasContentIid(permissionType: BennuMapperCompanion[_], permission: Permission, iid: InternalId): Boolean = {
    reachableContentIids(permissionType, permission).contains(iid)
  }

  def hasPermission(permissionType: BennuMapperCompanion[_], permission: Permission): Boolean = {
    getAcls.exists(_.role.hasPermission(permissionType, permission))
  }

  def hasRole(role: Role): Boolean = {
    getAcls.exists(_.role == role)
  }

  //TODO: call from Grant Access, Revoke Access
  def invalidateAcls(): Unit = synchronized { aclsLoaded = false }
  //TODO: call from Create Alias, Create Label, Move Label, Copy Label, Remove Label
  def invalidateLabels(): Unit = getAcls.foreach(_.invalidateLabels())
  def invalidateAliases(): Unit = getAcls.foreach(_.invalidateAliases())
  //TODO: call from Delete Connection, Complete Introduction
  def invalidateConnections(): Unit = getAcls.foreach(_.invalidateConnections())
  //TODO: call from Create Content, Add Content Label, Remove Content Label, Respond to Verification Request, Verify
  def invalidateContent(): Unit = getAcls.foreach(_.invalidateContent())

  private def getAcls: List[Acl] = {
    synchronized {
      if (!aclsLoaded) {
        AgentSecurityContext(agentId) {
          _acls = LabelAcl.selectAll.map(new Acl(_, injector)).toList
        }

        aclsLoaded = true
      }

      _acls
    }
  }
}
