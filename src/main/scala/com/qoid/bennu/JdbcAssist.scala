package com.qoid.bennu


import com.qoid.bennu.model.HasInternalId
import com.qoid.bennu.model.id.InternalId
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.Types
import m3.jdbc.ColumnMapper.SingleColumnMapper
import m3.jdbc._
import m3.json.JsonSerializer
import m3.predef._
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

    override def insert(instance: T)(implicit conn: Connection): T = {
      val i = super.insert(instance)
      postInsert(i)
      i
    }

    override def update(instance: T)(implicit conn: Connection): T = {
      val i = super.update(instance)
      postUpdate(i)
      i
    }

    def softDelete(instance: T)(implicit conn: Connection): T = {
      val i = super.update(instance.copy2(deleted=true).asInstanceOf[T])
      postDelete(i)
      i
    }

    def postInsert(instance: T): Unit = {}
    def postUpdate(instance: T): Unit = {}
    def postDelete(instance: T): Unit = {}

    def fromJson(jv: JValue): T = serializer.fromJsonTi(jv, TypeInfo(mapper.clazz))

    implicit def toJson(t: T): JValue = serializer.toJsonTi(t, TypeInfo(t.getClass))
    
    implicit def implicitMapper = this
  }

  trait BennuMappedInstance[T <: HasInternalId] extends Mapper.MappedInstance[T,InternalId] { self: T =>
  }
 
  lazy val allMappers = List[BennuMapperCompanion[_ <: HasInternalId]](
    model.Agent,
    model.Alias,
    model.Connection,
    model.Content,
    model.Introduction,
    model.Label,
    model.LabelAcl,
    model.LabelChild,
    model.LabeledContent,
    model.Notification,
    model.Profile
  )

  def findMapperByTypeName(typeName: String): BennuMapperCompanion[_ <: HasInternalId] = {
    allMappers.find(_.typeName =:= typeName).getOrError(s"don't know how to handle type ${typeName}")
  }

  def findMapperByType[T <: HasInternalId : Manifest]: BennuMapperCompanion[T] = {
    findMapperByTypeName(manifest[T].runtimeClass.getSimpleName).asInstanceOf[BennuMapperCompanion[T]]
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
