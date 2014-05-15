package com.qoid.bennu.schema

import com.qoid.bennu.AgentManager
import m3.Txn
import m3.jdbc._
import m3.predef._

object CreateDatabase extends App {

  import AuditAssist._
  import Settings._

  val injector = inject[ScalaInjector]

  Txn {
    dropTables()
    dropAuditTables()

    val ddl = schemaManager.createFullSchemaDdl ++ createFullAuditSchemaDdl ++ createAuditTriggerDdl
    ddl.foreach(conn.update(_))

    //findFile(s"extra-ddl-${dialectName}.sql").foreach(_.readText.splitList(";;;").foreach(conn.update(_)))

    val agentMgr = injector.instance[AgentManager]

    agentMgr.createIntroducerAgent()
  }
}
