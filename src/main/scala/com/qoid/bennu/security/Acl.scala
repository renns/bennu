package com.qoid.bennu.security

import java.sql.{Connection => JdbcConn}

import com.qoid.bennu.model.Alias
import com.qoid.bennu.model.Connection
import com.qoid.bennu.model.LabelAcl
import com.qoid.bennu.model.LabeledContent
import com.qoid.bennu.model.id.InternalId
import m3.jdbc._
import m3.predef._

class Acl(val labelAcl: LabelAcl, injector: ScalaInjector) {
  private var labelsLoaded = false
  private var aliasesLoaded = false
  private var connectionsLoaded = false
  private var contentLoaded = false

  private var _reachableLabelIids = List.empty[InternalId]
  private var _reachableAliasIids = List.empty[InternalId]
  private var _reachableConnectionIids = List.empty[InternalId]
  private var _reachableContentIids = List.empty[InternalId]

  def reachableLabelIids: List[InternalId] = {
    synchronized {
      loadLabelIids()
      _reachableLabelIids
    }
  }

  def reachableAliasIids: List[InternalId] = {
    synchronized {
      loadLabelIids()
      loadAliasIids()
      _reachableAliasIids
    }
  }

  def reachableConnectionIids: List[InternalId] = {
    synchronized {
      loadLabelIids()
      loadAliasIids()
      loadConnectionIids()
      _reachableConnectionIids
    }
  }

  def reachableContentIids: List[InternalId] = {
    synchronized {
      loadLabelIids()
      loadContentIids()
      _reachableContentIids
    }
  }

  def invalidateLabels(): Unit = {
    synchronized {
      labelsLoaded = false
      aliasesLoaded = false
      connectionsLoaded = false
      contentLoaded = false
    }
  }

  def invalidateAliases(): Unit = {
    synchronized {
      aliasesLoaded = false
      connectionsLoaded = false
    }
  }

  def invalidateConnections(): Unit = {
    synchronized {
      connectionsLoaded = false
    }
  }

  def invalidateContent(): Unit = {
    synchronized {
      contentLoaded = false
    }
  }

  private def loadLabelIids(): Unit = {
    if (!labelsLoaded) {
      // The following is executed outside of any security context
      _reachableLabelIids = injector.instance[JdbcConn].queryFor[InternalId](sql"""
        with recursive reachable_labels as (
          select iid as labelIid from label where iid = ${labelAcl.labelIid}
          union all
          select lc.childIid as labelIid
          from labelchild lc
            join reachable_labels lt on lt.labelIid = lc.parentIid
        )
        select * from reachable_labels
      """).toList

      labelsLoaded = true
    }
  }

  private def loadAliasIids(): Unit = {
    if (!aliasesLoaded) {
      AgentSecurityContext(labelAcl.agentId) {
        _reachableAliasIids = Alias.select(sql"labelIid in (${_reachableLabelIids})").map(_.iid).toList
      }

      aliasesLoaded = true
    }
  }

  private def loadConnectionIids(): Unit = {
    if (!connectionsLoaded) {
      AgentSecurityContext(labelAcl.agentId) {
        _reachableConnectionIids = Connection.select(sql"aliasIid in (${_reachableAliasIids})").map(_.iid).toList
      }

      connectionsLoaded = true
    }
  }

  private def loadContentIids(): Unit = {
    if (!contentLoaded) {
      AgentSecurityContext(labelAcl.agentId) {
        _reachableContentIids = LabeledContent.select(sql"labelIid in (${_reachableLabelIids})").map(_.contentIid).toList
      }

      contentLoaded = true
    }
  }
}
