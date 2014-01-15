package com.qoid.bennu

import m3.jdbc._
import java.sql.Connection
import com.qoid.bennu.model.HasInternalId
import com.qoid.bennu.model.InternalId

object JdbcAssist {

  implicit val columnMapper = m3.jdbc.ColumnMapper.defaultColumnMapperFactory


  trait BennuMapperCompanion[T <: HasInternalId] extends Mapper.MapperCompanion[T,InternalId] {
    def softDeleteViaKey(iid: InternalId)(implicit conn: Connection): Unit = {
      conn.update(s"update ${tableName.rawSql} set deleted = true where iid = ${iid}")
    }
    def softDelete(t: T)(implicit conn: Connection): T = {
      softDeleteViaKey(t.iid)
      t
    }
  }


  trait BennuMappedInstance[T <: HasInternalId] extends Mapper.MappedInstance[T,InternalId] { self: T =>
    override def mapper: BennuMapperCompanion[T]
    def softDelete(implicit conn: Connection): Unit = mapper.softDelete(this)
  }


}
