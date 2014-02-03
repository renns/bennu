package com.qoid.bennu

import com.qoid.bennu.model.HasInternalId
import com.qoid.bennu.model.InternalId
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.Types
import m3.jdbc.ColumnMapper.SingleColumnMapper
import m3.jdbc._
import m3.json.JsonSerializer
import m3.predef._
import m3.Txn
import m3.TypeInfo
import net.liftweb.json.JNothing
import net.liftweb.json.JValue

object JdbcAssist extends Logging {

  implicit val columnMapper = new ColumnMapper.DefaultColumnMapperFactory(jvalueColumnMapper :: ColumnMapper.mappers.allMapperFactories)


  trait BennuMapperCompanion[T <: HasInternalId[T]] extends Mapper.MapperCompanion[T,InternalId] { mapper =>
    
    val serializer = inject[JsonSerializer]
    
    def softDeleteViaKey(iid: InternalId)(implicit conn: Connection): Unit = {
      conn.update(sql"update ${tableName.rawSql} set deleted = true where iid = ${iid}")
    }
    def softDelete(t: T)(implicit conn: Connection): T = {
      Txn {
        softDeleteViaKey(t.iid)
        fetch(t.iid)
      }
    }
    def fromJson(jv: JValue): T = serializer.fromJsonTi(jv, TypeInfo(mapper.clazz))
    def toJson(t: T): JValue = serializer.toJsonTi(t, TypeInfo(t.getClass))
  }

  trait BennuMappedInstance[T <: HasInternalId[T]] extends Mapper.MappedInstance[T,InternalId] { self: T =>
    override def mapper: BennuMapperCompanion[T]
    def softDelete(implicit conn: Connection): Unit = mapper.softDelete(this)
  }

  def findMapperByTypeName(_type: String): BennuMapperCompanion[_ <: HasInternalId[_]] = {
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
  
  object jvalueColumnMapper extends SingleColumnMapper[JValue] {
    override val defaultValue = JNothing
    def sqlType = Types.VARCHAR
    override def fromResultSet(rs: ResultSet, col: Int) = rs.getString(col) match {
      case _ if rs.wasNull => JNothing
      case s => try {
        JsonAssist.parseJson(s)
      } catch {
        case e: Exception => {
          logger.warn(s"error parsing -- ${s}", e)
          JNothing
        }
      }
    }
    override def toPreparedStatement(ps: PreparedStatement, col: Int, value: JValue) = {
      val s = JsonAssist.prettyPrint(value)
      ps.setString(col, s)
    }
  }

}
