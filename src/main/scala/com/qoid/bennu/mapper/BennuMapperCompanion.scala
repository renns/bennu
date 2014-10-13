package com.qoid.bennu.mapper

import com.qoid.bennu.BennuException
import com.qoid.bennu.ErrorCode
import com.qoid.bennu.model.id.InternalId
import com.qoid.bennu.query.ast.Node
import com.qoid.bennu.query.ast.Query
import com.qoid.bennu.query.ast.Transformer
import com.qoid.bennu.query.QueryManager
import com.qoid.bennu.query.StandingQueryAction
import com.qoid.bennu.security.SecurityContext
import java.sql.{Connection => JdbcConn}
import m3.CaseClassReflector
import m3.Chord
import m3.jdbc.mapper.Mapper
import m3.jdbc.mapper.MapperFactory
import m3.jdbc.mapper.MapperInternal
import m3.predef._
import m3.predef.box._
import net.model3.chrono.DateTime
import net.model3.lang.ClassX

/** The base class for all mapper companion objects */
class BennuMapperCompanion[T <: BennuMappedInstance[T]](implicit mT: Manifest[T]) { self =>

  /** Returns the security context currently in scope */
  private def securityContext = inject[SecurityContext]

  /** Returns the JDBC connection currently in scope */
  private def jdbcConn = inject[JdbcConn]

  /** The query transformer to use when performing a select */
  protected val queryTransformer: PartialFunction[Node, Chord] = PartialFunction.empty

  private val queryMgr = inject[QueryManager]

  /** The internal mapper companion */
  private val mapper = new Mapper.MapperCompanion[T, InternalId] { mapper =>
    override val internalMapper = new MapperInternal[T, InternalId] {
      override def caseClassReflector = CaseClassReflector[T](mT)
      override def mapperFactory = inject[MapperFactory]

      override def select(where: String, maxRows: Int = -1)(implicit conn: JdbcConn): Iterator[T] = {
        // Constrict query so that only allowed results are returned
        val query = securityContext.constrictQuery(self, Query.parse(where))

        // Transform query object to a SQL string
        val querySql = Transformer.queryToSql(query, queryTransformer).toString()

        // Do the select
        super.select(querySql, maxRows)
      }
    }
  }

  /** The name of the table in the database */
  lazy val tableName = mapper.asMapperInternal.sqlSafeTableName

  /** The name of the mapper class */
  lazy val typeName = ClassX.getShortName(mapper.asMapperInternal.clazz)

  def select(where: String, maxRows: Int = -1): Iterator[T] = mapper.select(where, maxRows)(jdbcConn)
  def selectAll: Iterator[T] = mapper.selectAll(jdbcConn)
  def selectOne(where: String): T = mapper.selectOne(where)(jdbcConn)
  def selectOpt(where: String): Option[T] = mapper.selectOpt(where)(jdbcConn)
  def selectBox(where: String): Box[T] = mapper.selectBox(where)(jdbcConn)
  def fetch(key: InternalId): T = mapper.fetch(key)(jdbcConn)
  def fetchOpt(key: InternalId): Option[T] = mapper.fetchOpt(key)(jdbcConn)
  def fetchBox(key: InternalId): Box[T] = mapper.fetchBox(key)(jdbcConn)

  /** Performs an insert without modifying of audit data. */
  def rawInsert(instance: T): T = {
    // Set agentId
    val instance2 = instance.copy2(agentId = securityContext.agentId)

    // Check if an insert is allowed
    if (!securityContext.canInsert(instance2)) {
      throw new BennuException(ErrorCode.permissionDenied, "insert " + typeName)
    }

    // Do the insert
    mapper.insert(instance2)(jdbcConn)
  }

  /** Performs an insert */
  def insert(instance: T): T = {
    // Set agentId and audit fields
    val instance2 = instance.copy2(
      agentId = securityContext.agentId,
      created = new DateTime,
      modified = new DateTime,
      createdByConnectionIid = securityContext.connectionIid,
      modifiedByConnectionIid = securityContext.connectionIid
    )

    // Check if an insert is allowed
    if (!securityContext.canInsert(instance2)) {
      throw new BennuException(ErrorCode.permissionDenied, "insert " + typeName)
    }

    // Do the insert
    val instance3 = mapper.insert(instance2)(jdbcConn)

    // Notify standing queries
    queryMgr.notifyStandingQueries(instance3, StandingQueryAction.Insert)

    instance3
  }

  /** Performs an update */
  def update(instance: T): T = {
    // Set audit fields
    val instance2 = instance.copy2(
      modified = new DateTime,
      modifiedByConnectionIid = securityContext.connectionIid
    )

    // Check if an update is allowed
    if (!securityContext.canUpdate(instance2)) {
      throw new BennuException(ErrorCode.permissionDenied, "update " + typeName)
    }

    // Do the update
    val instance3 = mapper.update(instance2)(jdbcConn)

    // Notify standing queries
    queryMgr.notifyStandingQueries(instance3, StandingQueryAction.Update)

    instance3
  }

  /** Performs a delete */
  def delete(instance: T): T = {
    // Set audit fields
    val instance2 = instance.copy2(
      modified = new DateTime,
      modifiedByConnectionIid = securityContext.connectionIid
    )

    // Check if a delete is allowed
    if (!securityContext.canDelete(instance2)) {
      throw new BennuException(ErrorCode.permissionDenied, "delete " + typeName)
    }

    // Do the delete (after updating the audit fields)
    val instance3 = mapper.delete(mapper.update(instance2)(jdbcConn))(jdbcConn)

    // Notify standing queries
    queryMgr.notifyStandingQueries(instance3, StandingQueryAction.Delete)

    instance3
  }
}
