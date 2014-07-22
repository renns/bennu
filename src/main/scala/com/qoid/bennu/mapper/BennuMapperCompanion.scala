package com.qoid.bennu.mapper

import java.sql.{Connection => JdbcConn}

import com.qoid.bennu.model.id.InternalId
import com.qoid.bennu.query.ast.Node
import com.qoid.bennu.query.ast.Query
import com.qoid.bennu.query.ast.Transformer
import com.qoid.bennu.security.SecurityContext
import com.qoid.bennu.BennuException
import com.qoid.bennu.ErrorCode
import m3.Chord
import m3.jdbc._
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

  /** The factory that maps columns to fields */
  implicit val columnMapperFactory = new ColumnMapper.DefaultColumnMapperFactory(JValueColumnMapper :: ColumnMapper.mappers.allMapperFactories)

  /** The internal mapper companion */
  private val mapper = new Mapper.MapperCompanion[T, InternalId] { mapper =>
    /** Performs a query and returns the results in a collection */
    override def select(where: String, maxRows: Int = -1)(implicit conn: JdbcConn): Iterator[T] = {
      // Constrict query so that only allowed results are returned
      val query = securityContext.constrictQuery(self, Query.parse(where))

      // Transform query object to a SQL string
      val querySql = Transformer.queryToSql(query, queryTransformer).toString()

      // Do the select
      super.select(querySql, maxRows)
    }
  }

  /** The name of the table in the database */
  lazy val tableName = mapper.tableName

  /** The name of the mapper class */
  lazy val typeName = ClassX.getShortName(mapper.clazz)

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
    mapper.insert(instance2)(jdbcConn)
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
    mapper.update(instance2)(jdbcConn)
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
    mapper.delete(mapper.update(instance2)(jdbcConn))(jdbcConn)
  }
}
