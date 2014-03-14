package com.qoid.bennu.security

import com.qoid.bennu.JdbcAssist.BennuMapperCompanion
import com.qoid.bennu.model.HasInternalId
import com.qoid.bennu.model.InternalId
import com.qoid.bennu.model.Label
import com.qoid.bennu.squery.StandingQueryAction
import com.qoid.bennu.squery.ast.ContentQuery
import com.qoid.bennu.squery.ast.Query
import com.qoid.bennu.squery.ast.Transformer
import com.qoid.bennu.{ErrorCode, ServiceException}
import java.sql.{ Connection => JdbcConn }
import m3.jdbc._
import m3.predef._
import m3.predef.box._

object AgentView {
  val notDeleted = Query.parse("deleted = false")
}

trait AgentView {

  def validateInsert[T <: HasInternalId](t: T): Box[T] = validateInsertUpdateOrDelete(t)
  def validateUpdate[T <: HasInternalId](t: T): Box[T] = validateInsertUpdateOrDelete(t)
  def validateDelete[T <: HasInternalId](t: T): Box[T] = validateInsertUpdateOrDelete(t)

  def securityContext: SecurityContext

  def rootLabel: Label

  /**
   * Ensure that t is can be inserted, updated and deleted from this view
   */
  def validateInsertUpdateOrDelete[T <: HasInternalId](t: T): Box[T]

  /**
   * Can remove data that should be hidden at this view or outright deny access.
   * readResolve is called on data that has already been constricted
   */
  def readResolve[T <: HasInternalId](t: T): Box[T]

  def constrict[T <: HasInternalId](mapper: BennuMapperCompanion[T], query: Query): Query

  def insert[T <: HasInternalId](instance: T)(implicit mapper: BennuMapperCompanion[T]): T = {
    validateInsert(instance) match {
      case Full(i) => mapper.insert(i)(inject[JdbcConn]).notifyStandingQueries(StandingQueryAction.Insert).asInstanceOf[T]
      case _ => throw ServiceException("Security validation failed", ErrorCode.SecurityValidationFailed)
    }
  }

  def update[T <: HasInternalId](instance: T)(implicit mapper: BennuMapperCompanion[T]): T = {
    validateUpdate(instance) match {
      case Full(i) => mapper.update(i)(inject[JdbcConn]).notifyStandingQueries(StandingQueryAction.Update).asInstanceOf[T]
      case _ => throw ServiceException("Security validation failed", ErrorCode.SecurityValidationFailed)
    }
  }

  def delete[T <: HasInternalId](instance: T)(implicit mapper: BennuMapperCompanion[T]): T = {
    validateDelete(instance) match {
      case Full(i) => mapper.softDelete(i)(inject[JdbcConn]).notifyStandingQueries(StandingQueryAction.Delete).asInstanceOf[T]
      case _ => throw ServiceException("Security validation failed", ErrorCode.SecurityValidationFailed)
    }
  }

  def select[T <: HasInternalId](queryStr: String)(implicit mapper: BennuMapperCompanion[T]): Iterator[T] = {
    val query = constrict(
      mapper,
      Query.parse(queryStr).and(AgentView.notDeleted.expr)
    )
    val querySql = Transformer.queryToSql(query, ContentQuery.transformer).toString()
    mapper.
      select(querySql)(inject[JdbcConn])
  }

  def fetchOpt[T <: HasInternalId](key: InternalId)(implicit mapper: BennuMapperCompanion[T]): Option[T] = {
    select(sql"iid = $key").toList match {
      case x :: _ => Some(x)
      case _ => None
    }
  }

  def fetch[T <: HasInternalId](key: InternalId)(implicit mapper: BennuMapperCompanion[T]): T = {
    fetchOpt(key) match {
      case None => sys.error(s"key $key not found in ${mapper.tableName}")
      case Some(x) => x
    }
  }

  def fetchBox[T <: HasInternalId](key: InternalId)(implicit mapper: BennuMapperCompanion[T]): Box[T] = {
    fetchOpt(key) ?~ s"key $key not found in ${mapper.tableName}"
  }

  /**
   * Takes a label path from the root to the label so for A->B->C this will return C's Label.
   */
  def resolveLabel(path: List[String])(implicit jdbcConn: JdbcConn): Box[Label] = {

    def recurse(parentLabel: Label, path: List[String]): Box[Label] = {
      path match {
        case Nil => Full(parentLabel)
        case hd :: tl => parentLabel.findChild(hd).flatMap(ch=>recurse(ch, tl))
      }
    }

    if ( rootLabel.name == path.head ) recurse(rootLabel, path.tail) ?~ s"label not found -- ${path.mkString("/")}"
    else recurse(rootLabel, path) ?~ s"label not found -- ${path.mkString("/")}"
  }
}
