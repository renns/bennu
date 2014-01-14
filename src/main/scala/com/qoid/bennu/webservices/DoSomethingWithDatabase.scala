package com.qoid.bennu.webservices

import com.google.inject.Inject
import java.sql.Connection
import m3.predef._
import m3.jdbc._
import m3.json.Streamer._

/**
 * A demo showing something a little cooler, database io with the json streaming api (aka the lazy json api)
 * 
 * So this is reminiscent of haskell's lazy json streaming.  The JsArr is instantiated
 * but doesn't actually run through every thing at instantiation.  DoSomethingWithDatabase.service
 * returns the JsArr and the servlet beans ResponseFactory takes any JsVal instances returned 
 * and streams them lazily.  I.e. only one row of data is ever in memory at a time.
 * 
 * So this code could stream millions of records with a very low memory profile and very high performance,
 * as fast as the database can give us the data.
 * 
 * To say it another way.  The actual row loop doesn't get called right away.  It is called
 * later when the JsArr instance is serialized to the underlying ServletOutputStream.
 * 
 * Worth noting that since the action is lazy there is no way to provide any kind of on close code.  
 * That is the bad news.  The good news is the code registers all the cleanup needed with the transaction
 * manager and "just does the right thing(tm)".  That being when the transaction is complete (aka at the transaction boundary which in this case
 * is tied to the request) the transaction's completion code runs the various registered listeners and 
 * in there is the cleanup code for the result set created here as well as the  connection injected here. 
 * 
 * Test with 
 * 
 *   curl -v http://localhost:8080/api/doSomethingWithDatabase
 * 
 */
case class DoSomethingWithDatabase @Inject() (
  conn: Connection
) extends Logging {

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