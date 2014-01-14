package com.qoid.bennu.schema


import net.model3.newfile.File
import m3.jdbc.DataSourceFactory
import java.sql.DriverManager
import m3.jdbc.meta.DatabaseMeta
import m3.jdbc.meta.DataTypes
import m3.Txn
import m3.jdbc._
import m3.jdbc.schemagen.Parser
import m3.jdbc.schemagen.DatabaseDialect

object SchemaGenerate extends App {

  import Settings._

  lazy val schemaDdl = {
    Txn {
      schemaManager.createFullSchemaDdl.mkString("\n","\n;\n","")
    }
  }
  
  new File("create-db-hsqldb.sql").write(schemaDdl)

  println
  println(schemaDdl)
  println
  
}

