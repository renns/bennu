package com.qoid.bennu

import m3.jdbc._
import java.sql.Connection
import com.qoid.bennu.model.HasInternalId
import com.qoid.bennu.model.InternalId
import m3.predef._
import m3.json.JsonSerializer
import net.liftweb.json.JValue
import m3.TypeInfo

object JdbcAssist {

  implicit val columnMapper = m3.jdbc.ColumnMapper.defaultColumnMapperFactory


  trait BennuMapperCompanion[T <: HasInternalId] extends Mapper.MapperCompanion[T,InternalId] {
    
    val serializer = inject[JsonSerializer]
    
    def softDeleteViaKey(iid: InternalId)(implicit conn: Connection): Unit = {
      conn.update(sql"update ${tableName.rawSql} set deleted = true where iid = ${iid}")
    }
    def softDelete(t: T)(implicit conn: Connection): T = {
      softDeleteViaKey(t.iid)
      t
    }
    def fromJson(jv: JValue): T = serializer.fromJson(jv)
    def toJson(t: T): JValue = serializer.toJsonTi(t, TypeInfo(t.getClass))
  }

  trait BennuMappedInstance[T <: HasInternalId] extends Mapper.MappedInstance[T,InternalId] { self: T =>
    override def mapper: BennuMapperCompanion[T]
    def softDelete(implicit conn: Connection): Unit = mapper.softDelete(this)
  }

  def findMapperByTypeName(_type: String): BennuMapperCompanion[_ <: HasInternalId] = {
    import model._
    _type.toLowerCase match {
      case "alias" => Alias
      case "connection" => model.Connection
      case "content" => Content
      case "label" => Label
      case "labelacl" => LabelAcl
      case "labelchild" => LabelChild
      case "labeledcontent" => LabeledContent
      case _ => m3x.error(s"don't know how to handle type ${_type}")
    }
  }

}
