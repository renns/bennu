package com.qoid.bennu.schema

import m3.Txn
import net.model3.newfile.File

object SchemaGenerate extends App {

  import Settings._

  lazy val schemaDdl = {
    Txn {
      schemaManager.createFullSchemaDdl.mkString("\n","\n;\n","")
    }
  }

  Txn {
    new File(s"./schema/create-db-${dialect}.sql").write(schemaDdl)
  }

  println()
  println(schemaDdl)
  println()
}
