package com.qoid.bennu.schema

import m3.Txn
import m3.predef._
import java.sql.Connection
import m3.jdbc._
import javax.sql.DataSource
import com.qoid.bennu.model.Agent
import com.qoid.bennu.model.AgentId
import com.qoid.bennu.model.InternalId
import com.qoid.bennu.JsonAssist._
import net.model3.newfile.Directory
import com.qoid.bennu.model.AgentId
import com.qoid.bennu.webservices.CreateAgent
import m3.fs._
import com.qoid.bennu.model.Alias

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
    
    schemaManager.createFullSchemaDdl.foreach { ddl =>
      conn.update(ddl)
    }
    
    file("bennu-extra-ddl.sql").readText.splitList(";;;").foreach { ddl =>
      conn.update(ddl)
    }

    CreateAgent()(
        conn = conn, 
        id = CreateAgent.introducerAgentId, 
        overWrite = true, 
        connectToIntroducer = false
    ).doCreate()
    
    val introducerAlias = Alias.fetch(Agent.fetch(CreateAgent.introducerAgentId.asIid).uberAliasIid) 
    // fix the name of the introducer's alias
    introducerAlias.copy(profile = CreateAgent.createProfile("introducer")).sqlUpdate

    CreateAgent()(conn, AgentId("007"), true, true).doCreate
    CreateAgent()(conn, AgentId("008"), true, true).doCreate

    conn.commit
    
  }
  
  Txn {
    inject[DataSource].getConnection.update("shutdown")
  }
  
}