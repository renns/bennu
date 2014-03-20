package com.qoid.bennu.security

import com.google.inject.Inject
import com.google.inject.Provider
import com.google.inject.Singleton
import com.qoid.bennu.model.id.AgentId
import com.qoid.bennu.model.id.InternalId
import java.sql.{ Connection => JdbcConn }
import m3.Txn
import m3.jdbc._
import m3.predef._
import m3.predef.box._
import m3.servlet.ForbiddenException
import m3.servlet.beans.Wrappers
import m3.servlet.longpoll.ChannelId
import m3.servlet.longpoll.GuiceProviders.ProviderOptionalChannelId
import net.model3.transaction.Transaction

object SecurityContext {

  @Singleton
  class ProviderSecurityContext @Inject() (
    provChannelId: Provider[Option[ChannelId]],
    provTxn: Provider[Transaction]
  ) extends Provider[SecurityContext] {

    val attrName = classOf[SecurityContext].getName
    def get: SecurityContext = {
      provTxn.get.getAttribute[SecurityContext](attrName, true) match {
        case null =>
          val channelId = provChannelId.get
          channelId.flatMap(chId => ChannelMap(chId)).getOrElse(throw new ForbiddenException(s"unable to find security context -- ${channelId}"))
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
        createFn = provSecurityContext.get.createView
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
      select iid as labelIid from label where iid = ${parentLabelIid} and deleted = false
    union all 
      -- the following is the recursion query
      select lc.childIid as labelIid
      from labelchild lc
         join reachable_labels lt on lt.labelIid = lc.parentIid
       where lc.deleted = false
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
