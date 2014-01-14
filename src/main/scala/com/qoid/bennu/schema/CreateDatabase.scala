package com.qoid.bennu.schema

import m3.Txn
import m3.predef._
import java.sql.Connection
import m3.jdbc._
import javax.sql.DataSource

object CreateDatabase extends App {

  import Settings._
  
  Txn {
    
    implicit val conn = inject[Connection]
    
    schemaManager.createFullSchemaDdl.foreach { ddl =>
      conn.update(ddl)
    }
    
    conn.commit
        
  }
  
  Txn {
    inject[DataSource].getConnection.update("shutdown")
  }
  
}