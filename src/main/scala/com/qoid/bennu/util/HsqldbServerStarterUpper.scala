package com.qoid.bennu.util

import com.google.inject.Singleton
import org.hsqldb.persist.HsqlProperties
import m3.predef._
import org.hsqldb.Server
import m3.jdbc.Database
import com.google.inject.Inject
import com.qoid.bennu.Config

@Singleton
class HsqldbServerStarterUpper @Inject() (
    database: Database,
    config: Config
) {

  if ( config.startHsqldbTooling && java.lang.Boolean.getBoolean("longLivedApp") )
    spawn("hsqldb-server-startup") {
      val p = new HsqlProperties()
      p.setProperty("server.database.0","file:db/main")
      p.setProperty("server.dbname.0","main")
      
      // set up the rest of properties
  
      // alternative to the above is
      val server = new Server()
      server.setProperties(p)
      server.setLogWriter(null) // can use custom writer
      server.setErrWriter(null) // can use custom writer
      server.start()
      
      org.hsqldb.util.DatabaseManager.main(Array("--url", database.jdbcUrl))
      
    }
 
}
