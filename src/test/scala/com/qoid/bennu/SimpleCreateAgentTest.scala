package com.qoid.bennu

import JsonAssist._
import com.qoid.bennu.model.Agent
import com.qoid.bennu.model.id.AgentId
import com.qoid.bennu.model.id.InternalId
import com.qoid.bennu.security.AgentSecurityContext
import com.qoid.bennu.security.SecurityContext
import java.sql.Connection
import m3.Txn
import m3.predef._
import org.junit.Test

class SimpleCreateAgentTest extends ScalaUnitTest {

  @Test def simpleCreateAgent() {
    
    Txn {
      implicit val conn = inject[Connection]
      val agentId = AgentId.random
      Txn.setViaTypename[SecurityContext](AgentSecurityContext(inject[ScalaInjector], agentId))

      val a0 = Agent.insert(Agent(
        iid = agentId.asIid,
        agentId = agentId,
        name = "Betty",
        data = JNothing
      ))

      val a1 = Agent.fetch(agentId.asIid)
      
      assert(a0 === a1)
    }
  }
}
