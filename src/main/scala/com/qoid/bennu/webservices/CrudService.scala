package com.qoid.bennu.webservices

import com.google.inject.Inject
import java.sql.Connection
import m3.jdbc._
import m3.json.Streamer._
import m3.predef._
import m3.servlet.beans.Parm

case class CrudService @Inject() (
  conn: Connection,
  @Parm("type") _type: String,
  @Parm action: String,
  @Parm json: String
) extends Logging {
/*
  Need to deserialize json into _type
  What are the available actions?
  Create
    Who generates the InternalId?
    If client does, do we need to check to make sure it isn't already in use?
  Read
    What would the json be?
    Is there a case where we only want to get one item (instead of a list)?
  Update
    Do we check to make sure the data exists?
  Delete
    What would the json be?
    What happens when something is deleted that others have a relationship to?
*/
  def service = {
    JsArr(
      conn.query("select * from TABLES") { row =>
        JsObj(
          // collect all the fields in a row
          (1 to row.getMetaData.getColumnCount) map { col =>
          // do some generic cleanup so that Int's look like javascript int's instead of strings
            val jsValue = row.getObject(col) match {
              case null => JsNull
              case n: Number => JsNum(n)
              case s: String => JsStr(s)
              case x => JsStr(x.toString)
            }
            JsFld(row.getMetaData.getColumnName(col) -> jsValue)
          }
        )
      }
    )
  }
}
