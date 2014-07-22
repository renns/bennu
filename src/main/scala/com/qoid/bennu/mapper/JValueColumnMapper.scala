package com.qoid.bennu.mapper

import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.Types

import com.qoid.bennu.JsonAssist
import m3.Logging
import m3.jdbc.ColumnMapper.SingleColumnMapper
import net.liftweb.json.JNothing
import net.liftweb.json.JValue

object JValueColumnMapper extends SingleColumnMapper[JValue] with Logging {
  override val defaultValue = JNothing

  def sqlType = Types.VARCHAR

  override def fromResultSet(rs: ResultSet, col: Int): JValue = {
    rs.getString(col) match {
      case _ if rs.wasNull => JNothing

      case s =>
        try {
          JsonAssist.parseJson(s)
        } catch {
          case e: Exception => {
            logger.warn(s"error parsing -- ${s}", e)
            JNothing
          }
        }
    }
  }

  override def toPreparedStatement(ps: PreparedStatement, col: Int, value: JValue): Unit = {
    val s = JsonAssist.prettyPrint(value)
    ps.setString(col, s)
  }
}
