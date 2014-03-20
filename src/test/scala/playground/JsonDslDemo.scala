package playground

import com.qoid.bennu.JsonAssist._
import com.qoid.bennu.model.Agent
import com.qoid.bennu.model.id.AgentId
import com.qoid.bennu.model.id.InternalId
import jsondsl._

object JsonDslDemo {

  val agentId = AgentId("007")
  
  val agent = Agent(
    agentId = agentId,
    uberAliasIid = InternalId.random,
    name = "bob"
  )
  
  val jv2 = ("a" -> agent) ~ ("b" -> List(agentId, agentId))
  
}

