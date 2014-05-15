package com.qoid.bennu.schema

import java.sql.Connection
import m3.Txn
import m3.fs._
import m3.jdbc._
import m3.jdbc.schemagen.DatabaseDialect._
import m3.jdbc.schemagen._
import m3.predef._
import m3.predef.box._
import net.model3.guice.LifeCycleManager

object Settings {
  
  inject[LifeCycleManager].config.fire()
  DataSourceFactory.toString
  
  def parse(s: String): Model = {
    val parser = new Parser
    parser.parse(s)
  }
  
  def findFile(filename: String): Box[LocalFileSystem.File] = {
    val prefixes = List("./", "./schema/", "./bin/")
    val possibles = prefixes.map(p=>file(p + filename))
    possibles.find(_.exists) ?~ s"unable to find file -- ${possibles.map(_.canonical).mkString("  ")}"
  }

  lazy implicit val conn = inject[Connection]

  lazy val schemaManager = SchemaManager(conn, findFile("bennu.schema").open_$)

  lazy val dialectName = schemaManager.dialect match {
    case _: HsqldbDialect => "hsqldb"
    case _: Postgres => "postgresql"
    case _ => "unknown"
  }

  def dropTables(): Unit = {
    schemaManager.model.resolvedTables.values.foreach { table =>
      conn.update(s"DROP TABLE IF EXISTS ${table.sqlName}")
    }
  }

  def printDdl(getDdlFn: => Iterable[String]) = {
    Txn {
      println(getDdlFn.mkString("\n","\n;\n",""))
    }
  }
}
