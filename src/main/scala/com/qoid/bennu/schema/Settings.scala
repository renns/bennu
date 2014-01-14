package com.qoid.bennu.schema

import net.model3.newfile.File
import m3.jdbc.schemagen.Parser
import m3.jdbc.DataSourceFactory
import m3.jdbc.schemagen.DatabaseDialect
import java.sql.DriverManager
import net.model3.logging.SimpleLoggingConfigurator
import m3.predef._
import net.model3.guice.LifeCycleManager
import m3.jdbc.meta.DatabaseMeta
import m3.jdbc.meta.DataTypes
import m3.fs._
import m3.jdbc.schemagen.SchemaManager
import m3.Txn
import m3.jdbc._
import m3.predef._
import java.sql.Connection

object Settings {
  
  inject[LifeCycleManager].config.fire()
  DataSourceFactory.toString
  
  def parse(s: String) = {
    val parser = new Parser
    parser.parse(s)
  }

  lazy val conn = inject[Connection]

  lazy val schemaManager = SchemaManager(conn, file("bennu.schema"))

  def printDdl(getDdlFn: => Iterable[String]) = {
    Txn {
      println(getDdlFn.mkString("\n","\n;\n",""))
    }
  }

  
}