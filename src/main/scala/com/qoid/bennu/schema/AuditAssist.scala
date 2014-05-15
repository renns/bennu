package com.qoid.bennu.schema

import m3.jdbc._
import m3.jdbc.schemagen.DatabaseDialect.Postgres
import m3.jdbc.schemagen.HsqldbDialect
import m3.jdbc.schemagen.Model._
import m3.predef._

object AuditAssist {

  import Settings._

  lazy val auditTables = {
    schemaManager.model.resolvedTables.values.map { table =>
      val newLines = List(
        Field("auditId", TypeDef("bigint"), List(Anno("key", None, Nil), Anno("autoinc", None, Nil))),
        Field("auditAction", TypeDef("char", Some((6, None))), Nil)
      )

      val lines = table.lines.filter {
        case _: Index => false
        case _ => true
      }.map {
        case field: Field => field.copy(annos = field.annos.filterNot(_.name =:= "key"))
        case line => line
      } ++ newLines

      val tablePart = TablePart(table.modelName + "_log", table.sqlName + "_log", lines)

      ResolvedTable(tablePart, schemaManager.model)
    }
  }

  def createFullAuditSchemaDdl: Iterable[String] = {
    auditTables.toList.sortBy(_.sqlName).flatMap { table =>
      table.creationSql(schemaManager.dialect) :: table.indexCreationSql(schemaManager.dialect).toList
    }
  }

  def dropAuditTables(): Unit = {
    auditTables.foreach { table =>
      conn.update(s"DROP TABLE IF EXISTS ${table.sqlName}")
    }
  }

  def createAuditTriggerDdl(): Iterable[String] = {
    val insert = TriggerAction("INSERT", "NEW")
    val update = TriggerAction("UPDATE", "NEW")
    val delete = TriggerAction("DELETE", "OLD")

    schemaManager.dialect match {
      case _: HsqldbDialect =>
        schemaManager.model.resolvedTables.values.foldLeft(List.empty[String]) { case (triggers, table) =>
          createHsqldbTrigger(table, insert) ::
          createHsqldbTrigger(table, update) ::
          createHsqldbTrigger(table, delete) ::
          triggers
        }
      case _: Postgres =>
        schemaManager.model.resolvedTables.values.foldLeft(List.empty[String]) { case (triggers, table) =>
          createPostgresTriggerFunction(table, insert) ::
          createPostgresTriggerFunction(table, update) ::
          createPostgresTriggerFunction(table, delete) ::
          createPostgresTrigger(table, insert) ::
          createPostgresTrigger(table, update) ::
          createPostgresTrigger(table, delete) ::
          triggers
        }
      case _ => m3x.error("Unsupported database dialect")
    }
  }

  private def createHsqldbTrigger(table: ResolvedTable, action: TriggerAction): String = {
    s"CREATE TRIGGER ${action.triggerName(table.sqlName)} AFTER ${action.name} ON ${table.sqlName}\n" +
    s"REFERENCING ${action.rowName} ROW AS ${action.rowName}\n" +
    s"FOR EACH ROW\n" +
    s"INSERT INTO ${table.sqlName}_log\n" +
    s"(\n" +
    table.resolvedFields.map("\t" + _.name).mkString("", ",\n", ",\n") +
    s"\tauditAction\n" +
    s") VALUES (\n" +
    table.resolvedFields.map("\t" + action.rowName + "." + _.name).mkString("", ",\n", ",\n") +
    s"\t'${action.name}'\n" +
    s")"
  }

  private def createPostgresTriggerFunction(table: ResolvedTable, action: TriggerAction): String = {
    s"CREATE OR REPLACE FUNCTION ${action.triggerName(table.sqlName)}() RETURNS TRIGGER AS $$$$\n" +
    s"BEGIN\n" +
    s"INSERT INTO ${table.sqlName}_log\n" +
    s"(\n" +
    table.resolvedFields.map("\t" + _.name).mkString("", ",\n", ",\n") +
    s"\tauditAction\n" +
    s") VALUES (\n" +
    table.resolvedFields.map("\t" + action.rowName + "." + _.name).mkString("", ",\n", ",\n") +
    s"\t'${action.name}'\n" +
    s");\n" +
    s"RETURN NULL;\n" +
    s"END;\n" +
    s"$$$$ LANGUAGE plpgsql"
  }

  private def createPostgresTrigger(table: ResolvedTable, action: TriggerAction): String = {
    s"CREATE TRIGGER ${action.triggerName(table.sqlName)} AFTER ${action.name} ON ${table.sqlName}\n" +
    s"FOR EACH ROW\n" +
    s"EXECUTE PROCEDURE ${action.triggerName(table.sqlName)}()"
  }

  case class TriggerAction(
    name: String,
    rowName: String
  ) {
    def triggerName(tableName: String): String = s"${tableName}_log_${name.toLowerCase}"
  }
}
