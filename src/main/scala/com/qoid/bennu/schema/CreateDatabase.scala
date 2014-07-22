package com.qoid.bennu.schema

import com.qoid.bennu.model.assist.AgentAssist
import com.qoid.bennu.model.id.AgentId
import com.qoid.bennu.model.id.InternalId
import com.qoid.bennu.security.AgentSecurityContext
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

    findFile(s"extra-ddl-${dialectName}.sql").foreach(_.readText.splitList(";;;").foreach(conn.update(_)))

    val agentMgr = injector.instance[AgentAssist]

    val agentId = AgentId.random
    val connectionIid = InternalId.random

    AgentSecurityContext(agentId, connectionIid) {
      agentMgr.createIntroducerAgent()
    }

    shutdownDatabase()
  }
}
