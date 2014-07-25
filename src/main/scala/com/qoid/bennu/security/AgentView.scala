package com.qoid.bennu.security

import com.qoid.bennu.ErrorCode
import com.qoid.bennu.JdbcAssist.BennuMapperCompanion
import com.qoid.bennu.ServiceException
import com.qoid.bennu.model._
import com.qoid.bennu.model.id.InternalId
import com.qoid.bennu.squery.ast.ContentQuery
import com.qoid.bennu.squery.ast.Query
import com.qoid.bennu.squery.ast.Transformer
import java.sql.{ Connection => JdbcConn }
import m3.jdbc._
import m3.predef._
import m3.predef.box._
import net.model3.chrono.DateTime

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

  def constrict[T <: HasInternalId](mapper: BennuMapperCompanion[T], query: Query): Query

  def insert[T <: HasInternalId](instance: T)(implicit mapper: BennuMapperCompanion[T]): T = {
    validateInsert(
      instance.copy2(
        agentId = securityContext.agentId,
        created = new DateTime,
        modified = new DateTime,
        createdByAliasIid = securityContext.aliasIid,
        modifiedByAliasIid = securityContext.aliasIid
      ).asInstanceOf[T]
    ) match {
      case Full(i) => mapper.insert(i)(inject[JdbcConn])
      case _ => throw ServiceException("Security validation failed", ErrorCode.SecurityValidationFailed)
    }
  }

  def update[T <: HasInternalId](instance: T)(implicit mapper: BennuMapperCompanion[T]): T = {
    validateUpdate(
      instance.copy2(
        agentId = securityContext.agentId,
        modified = new DateTime,
        modifiedByAliasIid = securityContext.aliasIid
      ).asInstanceOf[T]
    ) match {
      case Full(i) => mapper.update(i)(inject[JdbcConn])
      case _ => throw ServiceException("Security validation failed", ErrorCode.SecurityValidationFailed)
    }
  }

  def delete[T <: HasInternalId](instance: T)(implicit mapper: BennuMapperCompanion[T]): T = {
    validateDelete(
      instance.copy2(
        agentId = securityContext.agentId,
        modified = new DateTime,
        modifiedByAliasIid = securityContext.aliasIid
      ).asInstanceOf[T]
    ) match {
      case Full(i) => mapper.delete(i)(inject[JdbcConn])
      case _ => throw ServiceException("Security validation failed", ErrorCode.SecurityValidationFailed)
    }
  }

  def select[T <: HasInternalId](queryStr: String)(implicit mapper: BennuMapperCompanion[T]): Iterator[T] = {
    val query = constrict(
      mapper,
      Query.parse(queryStr)
    )
    val querySql = Transformer.queryToSql(query, ContentQuery.transformer).toString()
    mapper.select(querySql)(inject[JdbcConn])
  }

  def selectOpt[T <: HasInternalId](queryStr: String)(implicit mapper: BennuMapperCompanion[T]): Option[T] = {
    select[T](queryStr).toList.headOption
  }

  def selectBox[T <: HasInternalId](queryStr: String)(implicit mapper: BennuMapperCompanion[T]): Box[T] = {
    selectOpt[T](queryStr) ?~ s"no records returned from -- select * from ${mapper.asMapperInternal.sqlSafeTableName} where ${queryStr}"
  }

  def selectOne[T <: HasInternalId](queryStr: String)(implicit mapper: BennuMapperCompanion[T]): T = {
    selectOpt(queryStr) match {
      case None => m3x.error(s"expected one record got zero for query on ${mapper.asMapperInternal.sqlSafeTableName} -- ${queryStr}")
      case Some(t) => t
    }
  }

  def fetchOpt[T <: HasInternalId](key: InternalId)(implicit mapper: BennuMapperCompanion[T]): Option[T] = {
    select(sql"iid = $key").toList match {
      case x :: _ => Some(x)
      case _ => None
    }
  }

  def fetch[T <: HasInternalId](key: InternalId)(implicit mapper: BennuMapperCompanion[T]): T = {
    fetchOpt(key) match {
      case None => sys.error(s"key $key not found in ${mapper.asMapperInternal.sqlSafeTableName}")
      case Some(x) => x
    }
  }

  def fetchBox[T <: HasInternalId](key: InternalId)(implicit mapper: BennuMapperCompanion[T]): Box[T] = {
    fetchOpt(key) ?~ s"key $key not found in ${mapper.asMapperInternal.sqlSafeTableName}"
  }

  def findChildLabel(parentIid: InternalId, childName: String): Box[Label] = {
    Label.selectBox(sql"name = ${childName} and iid in (select childIid from LabelChild where parentIid = ${parentIid})")(inject[JdbcConn])
  }

  /**
   * Takes a label path from the root to the label so for A->B->C this will return C's Label.
   */
  def resolveLabel(path: List[String])(implicit jdbcConn: JdbcConn): Box[Label] = {

    def recurse(parentLabel: Label, path: List[String]): Box[Label] = {
      path match {
        case Nil => Full(parentLabel)
        case hd :: tl => findChildLabel(parentLabel.iid, hd).flatMap(ch=>recurse(ch, tl))
      }
    }

    if ( rootLabel.name == path.head ) recurse(rootLabel, path.tail) ?~ s"label not found -- ${path.mkString("/")}"
    else recurse(rootLabel, path) ?~ s"label not found -- ${path.mkString("/")}"
  }

  def resolveConnectionMetaLabel(): Box[InternalId] = Empty

  def hasAccessToAlias(aliasIid: InternalId): Boolean = false
}
