package com.qoid.bennu.schema

import m3.Txn
import m3.predef._
import java.sql.Connection
import m3.jdbc._
import javax.sql.DataSource
import com.qoid.bennu.model.Agent
import com.qoid.bennu.model.InternalId
import com.qoid.bennu.JsonAssist._
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
    
    schemaManager.createFullSchemaDdl.foreach { ddl =>
      conn.update(ddl)
    }
   
    Agent(
      iid = InternalId("007"),
      name = "Bond, James Bond",
      data = JNothing
    ).sqlInsert

    conn.commit
    
  }
  
  Txn {
    inject[DataSource].getConnection.update("shutdown")
  }
  
}