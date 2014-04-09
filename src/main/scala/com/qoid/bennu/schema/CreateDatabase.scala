package com.qoid.bennu.schema

import com.qoid.bennu.AgentManager
import java.sql.{ Connection => JdbcConn }
import javax.sql.DataSource
import m3.Txn
import m3.fs._
import m3.jdbc._
import m3.predef._
import net.model3.newfile.Directory

object CreateDatabase extends App {

  import Settings._

  val injector = inject[ScalaInjector]

  try {
    new Directory("./db/").deleteTree()
  } catch {
    case th: Throwable => {
      th.printStackTrace()
      println("error deleting existing database (see preceding stack trace), will still try to create a new database but this may need manual intervention to work")
    }
  }
  
  Txn {
    implicit val jdbcConn = injector.instance[JdbcConn]
    
    schemaManager.createFullSchemaDdl.foreach(conn.update(_))

    findFile("bennu-extra-ddl.sql").readText.splitList(";;;").foreach(conn.update(_))

    val agentMgr = injector.instance[AgentManager]

    agentMgr.createIntroducerAgent()

    conn.commit()
  }
  
  Txn {
    injector.instance[DataSource].getConnection.update("shutdown")
  }

  System.exit(0)
}
