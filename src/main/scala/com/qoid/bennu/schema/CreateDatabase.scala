package com.qoid.bennu.schema

import com.qoid.bennu.model._
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

    findFile("bennu-extra-ddl.sql").readText.splitList(";;;").foreach(conn.update(_))

    CreateAgent(
      injector = inject[ScalaInjector],
      name = CreateAgent.introducerAgentName,
      overWrite = true,
      connectToIntroducer = false
    ).doCreate()

    val introducerAgent = Agent.selectOne(sql"name = ${CreateAgent.introducerAgentName}")
    val introducerAlias = Alias.fetch(introducerAgent.uberAliasIid)
    val introducerLabel = Label.fetch(introducerAlias.rootLabelIid)
    val introducerProfile = Profile.selectOne(sql"aliasIid = ${introducerAlias.iid}")

    // fix the name of the introducer's alias
    introducerAlias.copy(name = CreateAgent.introducerAliasName).sqlUpdate
    introducerLabel.copy(name = CreateAgent.introducerAliasName).sqlUpdate
    introducerProfile.copy(name = CreateAgent.introducerAliasName).sqlUpdate

    conn.commit()
  }
  
  Txn {
    inject[DataSource].getConnection.update("shutdown")
  }

  System.exit(0)
}
