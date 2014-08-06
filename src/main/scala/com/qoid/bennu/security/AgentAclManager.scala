package com.qoid.bennu.security

import com.qoid.bennu.mapper.BennuMapperCompanion
import com.qoid.bennu.model.LabelAcl
import com.qoid.bennu.model.id.AgentId
import com.qoid.bennu.model.id.InternalId
import m3.predef._

class AgentAclManager(agentId: AgentId, injector: ScalaInjector) {
  private var aclsLoaded: Boolean = false

  private var _acls = List.empty[Acl]

  def reachableLabelIids(connectionIid: InternalId, permissionType: BennuMapperCompanion[_], permission: Permission): List[InternalId] = {
    getAcls(connectionIid, permissionType, permission).flatMap(_.reachableLabelIids)
  }

  def reachableAliasIids(connectionIid: InternalId, permissionType: BennuMapperCompanion[_], permission: Permission): List[InternalId] = {
    getAcls(connectionIid, permissionType, permission).flatMap(_.reachableAliasIids)
  }

  def reachableConnectionIids(connectionIid: InternalId, permissionType: BennuMapperCompanion[_], permission: Permission): List[InternalId] = {
    getAcls(connectionIid, permissionType, permission).flatMap(_.reachableConnectionIids)
  }

  def reachableContentIids(connectionIid: InternalId, permissionType: BennuMapperCompanion[_], permission: Permission): List[InternalId] = {
    getAcls(connectionIid, permissionType, permission).flatMap(_.reachableContentIids)
  }

  def hasLabelIid(connectionIid: InternalId, permissionType: BennuMapperCompanion[_], permission: Permission, iid: InternalId): Boolean = {
    reachableLabelIids(connectionIid, permissionType, permission).contains(iid)
  }

  def hasAliasIid(connectionIid: InternalId, permissionType: BennuMapperCompanion[_], permission: Permission, iid: InternalId): Boolean = {
    reachableAliasIids(connectionIid, permissionType, permission).contains(iid)
  }

  def hasConnectionIid(connectionIid: InternalId, permissionType: BennuMapperCompanion[_], permission: Permission, iid: InternalId): Boolean = {
    reachableConnectionIids(connectionIid, permissionType, permission).contains(iid)
  }

  def hasContentIid(connectionIid: InternalId, permissionType: BennuMapperCompanion[_], permission: Permission, iid: InternalId): Boolean = {
    reachableContentIids(connectionIid, permissionType, permission).contains(iid)
  }

  def hasPermission(connectionIid: InternalId, permissionType: BennuMapperCompanion[_], permission: Permission): Boolean = {
    getAcls(connectionIid, permissionType, permission).nonEmpty
  }

  def invalidateAcls(): Unit = synchronized { aclsLoaded = false }
  def invalidateLabels(): Unit = getAcls().foreach(_.invalidateLabels())
  def invalidateAliases(): Unit = getAcls().foreach(_.invalidateAliases())
  def invalidateConnections(): Unit = getAcls().foreach(_.invalidateConnections())
  def invalidateContent(): Unit = getAcls().foreach(_.invalidateContent())

  private def getAcls(): List[Acl] = {
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

  def getAcls(connectionIid: InternalId): List[Acl] = {
    getAcls().filter(_.labelAcl.connectionIid == connectionIid)
  }

  def getAcls(connectionIid: InternalId, permissionType: BennuMapperCompanion[_], permission: Permission): List[Acl] = {
    getAcls(connectionIid).filter(_.labelAcl.role.hasPermission(permissionType, permission))
  }
}
