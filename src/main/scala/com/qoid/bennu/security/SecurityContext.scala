package com.qoid.bennu.security

import com.google.inject.Inject
import com.google.inject.Provider
import com.google.inject.Singleton
import com.qoid.bennu.model.id.AgentId
import com.qoid.bennu.model.id.InternalId
import com.qoid.bennu.session.Session
import com.qoid.bennu.session.SessionManager
import java.sql.{ Connection => JdbcConn }
import m3.Txn
import m3.jdbc._
import m3.predef._
import m3.servlet.beans.Wrappers
import m3.servlet.longpoll.ChannelId
import m3.servlet.longpoll.GuiceProviders.ProviderOptionalChannelId
import net.model3.transaction.Transaction

object SecurityContext {

  @Singleton
  class ProviderSession @Inject() (
    provChannelId: Provider[ChannelId],
    sessionMgr: SessionManager
  ) extends Provider[Session] {

    def get: Session = {
      sessionMgr.getSession(provChannelId.get())
    }
  }

  @Singleton
  class ProviderSecurityContext @Inject() (
    provSession: Provider[Session],
    provTxn: Provider[Transaction]
  ) extends Provider[SecurityContext] {

    val attrName = classOf[SecurityContext].getName
    def get: SecurityContext = {
      provTxn.get.getAttribute[SecurityContext](attrName, true) match {
        case null => provSession.get().securityContext
        case sc => sc
      }
    }
  }
  
  @Singleton
  class ProviderAgentView @Inject() (
    provSecurityContext: Provider[SecurityContext]
  ) extends Provider[AgentView] {

    def get: AgentView = {
      Txn.findOrCreate[AgentView](
        classOf[AgentView].getName, 
        createFn = provSecurityContext.get.createView,
        searchAncestors = false
      )
    }
  }

  class BennuProviderChannelId @Inject() (
    provOptChannelId: Provider[Option[ChannelId]]
  ) extends Provider[ChannelId] {

    def get = provOptChannelId.get.getOrError("unable to find channel id")
  }

  @Singleton
  class BennuProviderOptionChannelId @Inject() (
    provHttpReq: Provider[Option[Wrappers.Request]],
    provChannelId: ProviderOptionalChannelId
  ) extends Provider[Option[ChannelId]] {

    def get = {
      provHttpReq.
        get.
        flatMap { req =>
          req.parmValue("_channel").
            orElse(req.cookieValue("channel")).
            map(ChannelId.apply)
        }.
        orElse(provChannelId.get())
    }
  }

  def resolveLabelAncestry(parentLabelIid: InternalId)(implicit jdbcConn: JdbcConn): Iterator[InternalId] = {
    jdbcConn.queryFor[InternalId](sql"""
  with recursive reachable_labels as (
      select iid as labelIid from label where iid = ${parentLabelIid}
    union all 
      -- the following is the recursion query
      select lc.childIid as labelIid
      from labelchild lc
         join reachable_labels lt on lt.labelIid = lc.parentIid
  )
  select *
  from reachable_labels
    """)
  }
}

trait SecurityContext {
  def agentId: AgentId
  def aliasIid: InternalId
  def createView: AgentView
}
