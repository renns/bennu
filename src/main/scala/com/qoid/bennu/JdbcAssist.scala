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

    override def insert(instance: T)(implicit conn: Connection): T = postInsert(super.insert(preInsert(instance)))
    override def update(instance: T)(implicit conn: Connection): T = postUpdate(super.update(preUpdate(instance)))

    def softDelete(instance: T)(implicit conn: Connection): T = {
      postDelete(super.update(preDelete(instance.copy2(deleted = true).asInstanceOf[T])))
    }

    protected def preInsert(instance: T): T = instance
    protected def preUpdate(instance: T): T = instance
    protected def preDelete(instance: T): T = instance

    protected def postInsert(instance: T): T = instance
    protected def postUpdate(instance: T): T = instance
    protected def postDelete(instance: T): T = instance

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
