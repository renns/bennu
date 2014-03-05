package com.qoid.bennu.schema

import com.qoid.bennu.model.Agent
import com.qoid.bennu.model.Alias
import com.qoid.bennu.webservices.CreateAgent
import java.sql.Connection
import javax.sql.DataSource
import m3.Txn
import m3.fs._
import m3.jdbc._
import m3.predef._
import net.model3.newfile.Directory

object CreateDatabase extends App {

  import Settings._

  try {
    new Directory("./db/").deleteTree()
  } catch {
    case th: Throwable => {
      th.printStackTrace()
      println("error deleting existing database (see preceding stack trace), will still try to create a new database but this may need manual intervention to work")
    }
  }
  
  Txn {
    implicit val conn = inject[Connection]
    
    schemaManager.createFullSchemaDdl.foreach(conn.update(_))

    file("bennu-extra-ddl.sql").readText.splitList(";;;").foreach(conn.update(_))

    CreateAgent()(conn, CreateAgent.introducerAgentName, true, false).doCreate()
    
    val introducerAlias = Alias.fetch(Agent.selectOne(sql"name = ${CreateAgent.introducerAgentName}").uberAliasIid)

    // fix the name of the introducer's alias
    introducerAlias.copy(profile = CreateAgent.createProfile("introducer")).sqlUpdate

    CreateAgent()(conn, "007", true, true).doCreate()
    CreateAgent()(conn, "008", true, true).doCreate()

    conn.commit()
  }
  
  Txn {
    inject[DataSource].getConnection.update("shutdown")
  }
}
