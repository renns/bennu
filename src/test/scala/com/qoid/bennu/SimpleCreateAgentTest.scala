package com.qoid.bennu

import JsonAssist._
import com.qoid.bennu.model.Agent
import com.qoid.bennu.model.id.AgentId
import com.qoid.bennu.model.id.InternalId
import java.sql.Connection
import m3.Txn
import m3.predef._
import org.junit.Test

class SimpleCreateAgentTest extends ScalaUnitTest {

  
  @Test def simpleCreateAgent() {
    
    Txn {
      implicit val conn = inject[Connection]
      
      val iid = InternalId.random
      
      val a0 = Agent(
        iid = iid,
        uberAliasIid = InternalId.random,
        agentId = AgentId(iid.value),
        name = "Betty",
        data = JNothing
      )
      
      a0.sqlInsert
      
      val a1 = Agent.fetch(iid)
      
      assert(a0 === a1)
      
    }
    
  }

  
}
