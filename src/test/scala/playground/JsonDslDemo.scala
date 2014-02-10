package playground

import com.qoid.bennu.model.AgentId
import com.qoid.bennu.JsonAssist._
import jsondsl._
import com.qoid.bennu.model.Agent

object JsonDslDemo {

  val agentId = AgentId("007")
  
  val agent = Agent(
    agentId = agentId,
    name = "bob"
  )
  
  val jv2 = ("a" -> agent) ~ ("b" -> List(agentId, agentId))
  
}

