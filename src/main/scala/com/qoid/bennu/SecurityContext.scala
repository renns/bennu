package com.qoid.bennu

import com.qoid.bennu.model.AgentId
import com.qoid.bennu.model.InternalId
import com.google.inject.Provider
import com.google.inject.Inject
import m3.servlet.beans.Wrappers
import m3.servlet.longpoll.ChannelId
import m3.servlet.longpoll.ChannelManager
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
import m3.servlet.longpoll.GuiceProviders.ProviderOptionalChannelId
import com.google.inject.Singleton
import com.qoid.bennu.security.ChannelMap
import com.qoid.bennu.JdbcAssist.BennuMapperCompanion
import com.qoid.bennu.model.Alias

object SecurityContext {

  case object SuperUserSecurityContext extends SecurityContext {
    def createView = new AgentView {
      override def validateInsertUpdateOrDelete[T <: HasInternalId](t: T) = Full(t)
      def constrict[T <: HasInternalId](mapper: BennuMapperCompanion[T], query: Query): Query = query
      def readResolve[T <: HasInternalId](t: T) = Full(t)
    }
  }

  sealed trait AgentCapableSecurityContext extends SecurityContext {
    override def optAgentId: Option[AgentId] = Some(agentId)
    def agentId: AgentId
    lazy val agentWhereClause: Query = Query.parse(sql"agentId = ${agentId}")
  }

  case class AgentSecurityContext(agentId: AgentId) extends AgentCapableSecurityContext {
    def createView = new AgentView {
      override def validateInsertUpdateOrDelete[T <: HasInternalId](t: T) = {
        if ( agentId == t.agentId ) Full(t)
        else Failure(s"agent id of the object being validated ${t.agentId} is not the same as current agent in the security context")
      }
      override def constrict[T <: HasInternalId](mapper: BennuMapperCompanion[T], query: Query): Query = query.and(agentWhereClause.expr)
      override def readResolve[T <: HasInternalId](t: T) = {
        if ( agentId == t.agentId ) Full(t)
        else Failure("agent cannot read another agents data")
      }
    }
  }

  object ConnectionSecurityContext {
    import com.qoid.bennu.model._
    val readableTypes: Set[Mapper[_ <: HasInternalId, InternalId]] = Set(
      Content,
      Label,
      LabelAcl,
      LabelChild,
      LabeledContent
    )
  }
  
  case class AliasSecurityContext(aliasIid: InternalId) extends AgentCapableSecurityContext {
   
    lazy val agentId = Alias.fetch(aliasIid)(inject[JdbcConn]).agentId
    
    def createView = new AgentView {
  
      implicit val jdbcConn = inject[JdbcConn] 

      override def validateInsertUpdateOrDelete[T <: HasInternalId](t: T) = {
        if ( agentId == t.agentId ) Full(t)
        else Failure(s"agent id of the object being validated ${t.agentId} is not the same as current agent in the security context")
      }

      lazy val alias = Alias.fetch(aliasIid)

      lazy val reachableLabels = inject[JdbcConn].queryFor[InternalId](sql"""
  with recursive reachable_labels as (
     select rootLabelIid as labelIid from alias where iid = ${aliasIid}
     union all 
     -- the following is the recursion query
     select lc.childIid as labelIid
     from labelchild lc
       join reachable_labels lt on lt.labelIid = lc.parentIid
  )
  select *
  from reachable_labels
      """).toList
      
      val reachableLabelAcls = List[InternalId]()
      
      override def constrict[T <: HasInternalId](mapper: BennuMapperCompanion[T], query: Query): Query = {
        mapper.typeName.toLowerCase match {
          case "agent" => query.and(Query.parse(sql"""agentId = ${agentId}"""))
          case "alias" => query.and(Query.parse(sql"""iid = ${aliasIid}"""))
          case "label" => query.and(Query.parse(sql"""iid in (${reachableLabels})"""))
          case "content" => {
            // TODO this needs to be optimized
            val content = inject[JdbcConn].queryFor[InternalId](sql"""select contentIid from labeledcontent where labelIid in (${reachableLabels})""").toList
            query.and(Query.parse(sql"""iid in (${content})"""))
          }
          case "labelchild" => query.and(Query.parse(sql"""parentIid in (${reachableLabels}) and childIid in (${reachableLabels})"""))
          case "labeledcontent" => query.and(Query.parse(sql"""labelIid in (${reachableLabels})"""))
          case _ => query.and(Query.parse(sql"""1 <> 1"""))
        }
      }
      
      override def readResolve[T <: HasInternalId](t: T) = {
        import ConnectionSecurityContext._
        if ( agentId != t.agentId ) Failure("agent cannot read another agents data")
        else if ( readableTypes.contains(t.mapper.asInstanceOf[Mapper[_ <: HasInternalId, InternalId]]) ) Full(t)
        else {
          Empty
        }
      }
    }
    
  }
  
  case class ConnectionSecurityContext(connectionIid: InternalId) extends AgentCapableSecurityContext {
    
    lazy val agentId = Connection.fetch(connectionIid)(inject[JdbcConn]).agentId
    
    def createView = new AgentView {
  
      implicit val jdbcConn = inject[JdbcConn] 
      
      lazy val connection = Connection.fetch(connectionIid)
  
      override def validateInsertUpdateOrDelete[T <: HasInternalId](t: T) = Failure("connections cannot do insert, update or delete actions on other agents")

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
      
      override def constrict[T <: HasInternalId](mapper: BennuMapperCompanion[T], query: Query): Query = {
        
        if ( ConnectionSecurityContext.readableTypes.contains(mapper) ) {
          mapper.typeName.toLowerCase match {
            case "labelacl" => query.and(Query.parse(sql"""iid in (${reachableLabelAcls})"""))
            case "label" => query.and(Query.parse(sql"""iid in (${reachableLabels})"""))
            case "content" => query.and(Query.parse(sql"""iid in (select contentIid from labeledcontent where labelIid in (${reachableLabels}))"""))
            case "labelchild" => query.and(Query.parse(sql"""parentIid in (${reachableLabels}) and childIid in (${reachableLabels})"""))
            case "labeledcontent" => query.and(Query.parse(sql"""labelIid in (${reachableLabels})"""))
          }
        } else {
          throw new ServiceException(s"connection is not allowed to access ${mapper.typeName}", ErrorCode.Forbidden)
        }
      }
      
      override def readResolve[T <: HasInternalId](t: T) = {
        import ConnectionSecurityContext._
        if ( agentId != t.agentId ) Failure("agent cannot read another agents data")
        else if ( readableTypes.contains(t.mapper.asInstanceOf[Mapper[_ <: HasInternalId, InternalId]]) ) Full(t)
        else {
          Empty
        }
      }
    }

  }

  
  
  @Singleton
  class ProviderSecurityContext @Inject() (
      provChannelId: Provider[Option[ChannelId]]
  ) extends Provider[SecurityContext] {
    def get: SecurityContext = {
      provChannelId.get.flatMap(chId=>ChannelMap(chId)).getOrElse(throw new ForbiddenException("unable to find security context"))
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
        orElse(provChannelId.get)
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
  
  def validateInsert[T <: HasInternalId](t: T): Box[T] = validateInsertUpdateOrDelete(t)
  def validateUpdate[T <: HasInternalId](t: T): Box[T] = validateInsertUpdateOrDelete(t)
  def validateDelete[T <: HasInternalId](t: T): Box[T] = validateInsertUpdateOrDelete(t)
  
  /**
   * Ensure that t is can be inserted, updated and deleted from this view
   */
  def validateInsertUpdateOrDelete[T <: HasInternalId](t: T): Box[T]
  
  /**
   * Can remove data that should be hidden at this view or outright deny access.
   * 
   * readResolve is called on data that has already been constricted
   * 
   */
  def readResolve[T <: HasInternalId](t: T): Box[T]
  
  def constrict[T <: HasInternalId](mapper: BennuMapperCompanion[T], query: Query): Query

}

sealed trait SecurityContext {
  def optAgentId: Option[AgentId] = None
  def createView: AgentView 
}
