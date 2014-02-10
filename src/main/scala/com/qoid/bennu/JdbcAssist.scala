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
import net.model3.lang.ClassX
import scala.language.implicitConversions

object JdbcAssist extends Logging {

  implicit val columnMapper = new ColumnMapper.DefaultColumnMapperFactory(jvalueColumnMapper :: ColumnMapper.mappers.allMapperFactories)


  trait BennuMapperCompanion[T <: HasInternalId] extends Mapper.MapperCompanion[T,InternalId] { mapper =>
    
    lazy val typeName = ClassX.getShortName(clazz) 
    
    val serializer = inject[JsonSerializer]
    
    def softDeleteViaKey(iid: InternalId)(implicit conn: Connection): Unit = {
      conn.update(sql"update ${tableName.rawSql} set deleted = true where iid = ${iid}")
    }
    def softDelete(t: T)(implicit conn: Connection): T = {
      Txn {
        val i = t.copy2(deleted=true)
        t.mapper.update(i)
        i.asInstanceOf[T]
      }
    }
    def fromJson(jv: JValue): T = serializer.fromJsonTi(jv, TypeInfo(mapper.clazz))
    
    implicit def toJson(t: T): JValue = serializer.toJsonTi(t, TypeInfo(t.getClass))
    
  }

  trait BennuMappedInstance[T <: HasInternalId] extends Mapper.MappedInstance[T,InternalId] { self: T =>
    def softDelete(implicit conn: Connection): Unit = mapper.softDelete(cast)
  }
 
  lazy val allMappers = List(
    model.Agent,
    model.Alias,
    model.Connection,
    model.Content,
    model.Label,
    model.LabelAcl,
    model.LabelChild,
    model.LabeledContent,
    model.Notification
  )


  def findMapperByTypeName(typeName: String): BennuMapperCompanion[_ <: HasInternalId] = {
    import model._
    allMappers.find(_.typeName =:= typeName).getOrError(s"don't know how to handle type ${typeName}")
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
