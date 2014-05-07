package com.qoid.bennu.schema

import com.qoid.bennu.AgentManager
import java.sql.{ Connection => JdbcConn }
import m3.Txn
import m3.jdbc._
import m3.predef._

object CreateDatabase extends App {

  import Settings._

  val injector = inject[ScalaInjector]

  Txn {
    implicit val jdbcConn = injector.instance[JdbcConn]

    dropTables()

    schemaManager.createFullSchemaDdl.foreach(conn.update(_))

    findFile(s"extra-ddl-${dialect}.sql").foreach(_.readText.splitList(";;;").foreach(conn.update(_)))

    val agentMgr = injector.instance[AgentManager]

    agentMgr.createIntroducerAgent()
  }

  System.exit(0)
}
