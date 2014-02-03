package com.qoid.bennu

import com.qoid.bennu.model.AgentId
import com.qoid.bennu.model.InternalId
import com.google.inject.Provider
import com.google.inject.Inject
import m3.servlet.beans.Wrappers
import m3.servlet.longpoll.ChannelId
import m3.servlet.longpoll.ChannelManager
import m3.predef._
import com.qoid.bennu.model.Agent
import m3.servlet.ForbiddenException
import m3.servlet.NotFoundException
import m3.predef._
import box._
import com.qoid.bennu.model.HasInternalId
import com.qoid.bennu.squery.ast.Query
import m3.jdbc._
import com.qoid.bennu.model.HasInternalId
import java.sql.{ Connection => JdbcConn }
import com.qoid.bennu.model.Connection

object SecurityContext {

  case object SuperUserSecurityContext extends SecurityContext {
    def createView = new AgentView {
      override def validateInsertUpdateOrDelete[T <: HasInternalId[T]](t: T) = Full(t)
      def constrict[T <: HasInternalId[T]](mapper: Mapper[T,InternalId], query: Query): Query = query
      def readResolve[T <: HasInternalId[T]](t: T) = Full(t)
    }
  }

  sealed trait AgentCapableSecurityContext extends SecurityContext {
    override def optAgentId: Option[AgentId] = Some(agentId)
    def agentId: AgentId
    lazy val agentWhereClause: Query = Query.parse(sql"agentId = ${agentId}")
  }

  case class AgentSecurityContext(agentId: AgentId) extends AgentCapableSecurityContext {
    def createView = new AgentView {
      override def validateInsertUpdateOrDelete[T <: HasInternalId[T]](t: T) = {
        if ( agentId == t.agentId ) Full(t)
        else Failure(s"agent id of the object being validated ${t.agentId} is not the same as current agent in the security context")
      }
      override def constrict[T <: HasInternalId[T]](mapper: Mapper[T,InternalId], query: Query): Query = query.and(agentWhereClause.expr)
      override def readResolve[T <: HasInternalId[T]](t: T) = {
        if ( agentId == t.agentId ) Full(t)
        else Failure("agent cannot read another agents data")
      }
    }
  }

  object ConnectionSecurityContext {
    import com.qoid.bennu.model._
    val readableTypes: Set[Mapper[_ <: HasInternalId[_], InternalId]] = Set(
      Content,
      Label,
      LabelAcl,
      LabelChild,
      LabeledContent
    )
  }
  
  case class ConnectionSecurityContext(connectionIid: InternalId) extends AgentCapableSecurityContext {
    
    lazy val connection = Connection.fetch(connectionIid)(inject[JdbcConn])
    def agentId = connection.agentId
    
    def createView = new AgentView {
  
      implicit val jdbcConn = inject[JdbcConn] 
      
      lazy val connection = Connection.fetch(connectionIid)
  
      override def validateInsertUpdateOrDelete[T <: HasInternalId[T]](t: T) = Failure("connections cannot do insert, update or delete actions on other agents")

      lazy val reachableLabels = inject[JdbcConn].queryFor[InternalId](sql"""
  with recursive reachable_labels as (
     select labelIid
     from labelacl
     where connectionIid = ${connectionIid}  -- this is the starting point query
     union all 
     -- the following is the recursion query
     select lc.childIid
     from labelchild lc
       join reachable_labels lt on lt.labelIid = lc.parentIid
  )
  select *
  from reachable_labels
      """).toList
      
      lazy val reachableLabelAcls = inject[JdbcConn].queryFor[InternalId](sql"""
        select iid from labelacl where connectionIid = ${connectionIid}
      """).toList
      
      override def constrict[T <: HasInternalId[T]](mapper: Mapper[T,InternalId], query: Query): Query = ???
      
      override def readResolve[T <: HasInternalId[T]](t: T) = {
        import ConnectionSecurityContext._
        if ( agentId != t.agentId ) Failure("agent cannot read another agents data")
        else if ( readableTypes.contains(t.mapper) ) Full(t)
        else {
          Empty
        }
      }
    }

  }

  
  /**
   * Stupid implementation to convert a channelId into a SecurityContext.  Right now it assumes
   * all channels are agents.
   */
  def apply(channelId: ChannelId): SecurityContext = {
    val agentId = Agent.channelToAgentIdMap.get(channelId).getOrElse(throw new NotFoundException(s"no agent id found for ${channelId}"))
    AgentSecurityContext(agentId)
  }
  
  
  class ProviderSecurityContext @Inject() (
      provHttpReq: Provider[Option[Wrappers.Request]],
      provChannelId: Provider[Option[ChannelId]]
  ) extends Provider[SecurityContext] {
    def get: SecurityContext = {
      
      provHttpReq.
        get.
        flatMap { req =>
          req.parmValue("_channel").
            orElse(req.cookieValue("channel")).
            map(ChannelId.apply)
        }.
        orElse(provChannelId.get).
        map(chId=>SecurityContext(chId)).
        getOrElse(throw new ForbiddenException("unable to find a channel id that could be translated into an agent id"))
      
    }
  }

  class ProviderAgentCapableSecurityContext @Inject() (
      provSecurityContext: Provider[SecurityContext]
  ) extends Provider[AgentCapableSecurityContext] {
    def get: AgentCapableSecurityContext = {
      provSecurityContext.get match {
        case a: AgentCapableSecurityContext => a
        case s => m3x.error("found ${s} required AgentCapableSecurityContext")
      }
    }
  }
  
}


sealed trait AgentView {
  
  def validateInsert[T <: HasInternalId[T]](t: T): Box[T] = validateInsertUpdateOrDelete(t)
  def validateUpdate[T <: HasInternalId[T]](t: T): Box[T] = validateInsertUpdateOrDelete(t)
  def validateDelete[T <: HasInternalId[T]](t: T): Box[T] = validateInsertUpdateOrDelete(t)
  
  /**
   * Ensure that t is can be inserted, updated and deleted from this view
   */
  def validateInsertUpdateOrDelete[T <: HasInternalId[T]](t: T): Box[T]
  
  /**
   * Can remove data that should be hidden at this view or outright deny access.
   * 
   * readResolve is called on data that has already been constricted
   * 
   */
  def readResolve[T <: HasInternalId[T]](t: T): Box[T]
  
  def constrict[T <: HasInternalId[T]](mapper: Mapper[T,InternalId], query: Query): Query

}

sealed trait SecurityContext {
  def optAgentId: Option[AgentId] = None
  def createView: AgentView 
}
