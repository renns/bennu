package com.qoid.bennu


import com.qoid.bennu.model.HasInternalId
import com.qoid.bennu.model.id.InternalId
import com.qoid.bennu.squery.StandingQueryAction
import com.qoid.bennu.squery.StandingQueryManager
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.Types
import m3.TypeInfo
import m3.jdbc.mapper.ColumnMapper
import m3.jdbc.mapper.ColumnMapper.SingleColumnMapper
import m3.jdbc.mapper.Mapper
import m3.json.JsonSerializer
import m3.predef._
import m3.json.LiftJsonAssist._
import net.model3.lang.ClassX
import scala.language.implicitConversions

object JdbcAssist extends Logging {

  lazy val columnMapper = new ColumnMapper.DefaultColumnMapperFactory(jvalueColumnMapper :: ColumnMapper.mappers.allMapperFactories)


  trait BennuMapperCompanion[T <: HasInternalId] extends Mapper.MapperCompanion[T,InternalId] { mapper =>
    
    lazy val typeName = ClassX.getShortName(asMapperInternal.clazz)
    
    val serializer = inject[JsonSerializer]

    def rawInsert(instance: T)(implicit conn: Connection): T = super.insert(instance)

    override def insert(instance: T)(implicit conn: Connection): T = {
      postInsert(
        notifyStandingQueries(
          super.insert(
            preInsert(instance)
          ),
          StandingQueryAction.Insert
        )
      )
    }

    override def update(instance: T)(implicit conn: Connection): T = {
      postUpdate(
        notifyStandingQueries(
          super.update(
            preUpdate(instance)
          ),
          StandingQueryAction.Update
        )
      )
    }

    override def delete(instance: T)(implicit conn: Connection): T = {
      postDelete(
        super.delete(
          super.update(
            notifyStandingQueries(
              preDelete(instance),
              StandingQueryAction.Delete
            )
          )
        )
      )
    }

    protected def preInsert(instance: T): T = instance
    protected def preUpdate(instance: T): T = instance
    protected def preDelete(instance: T): T = instance

    protected def postInsert(instance: T): T = instance
    protected def postUpdate(instance: T): T = instance
    protected def postDelete(instance: T): T = instance

    def fromJson(jv: JValue): T = serializer.fromJsonTi(jv, TypeInfo(mapper.asMapperInternal.clazz))

    implicit def toJson(t: T): JValue = serializer.toJsonTi(t, TypeInfo(t.getClass))
    
    implicit def implicitMapper = this

    def notifyStandingQueries(instance: T, action: StandingQueryAction): T = {
//      inject[Transaction].events.addListener(new Transaction.Adapter {
//        override def commit(txn: Transaction) = {
//          SqueryEvalThread.enqueue(action, instance)
//        }
//      })
      inject[StandingQueryManager].notify(instance, action)
      instance
    }
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
    model.Login,
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
