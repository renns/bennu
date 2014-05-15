package com.qoid.bennu.schema

import m3.Txn
import net.model3.newfile.File

object SchemaGenerate extends App {

  import Settings._
  import AuditAssist._

  lazy val schemaDdl = {
    Txn {
      val ddl = schemaManager.createFullSchemaDdl ++ createFullAuditSchemaDdl ++ createAuditTriggerDdl
      ddl.mkString("\n","\n;\n","")
    }
  }

  Txn {
    new File(s"./schema/create-db-${dialectName}.sql").write(schemaDdl)
  }

  println()
  println(schemaDdl)
  println()
}
