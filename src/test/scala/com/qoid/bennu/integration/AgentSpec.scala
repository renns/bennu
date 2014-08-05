package com.qoid.bennu.integration

import com.qoid.bennu.client._
import com.qoid.bennu.model.id.InternalId
import org.specs2.Specification
import org.specs2.execute.Result

import scala.async.Async

class AgentSpec extends Specification {
  implicit val config = HttpClientConfig()

  def is = s2"""
    ${section("integration")}

    Agent should
      create agent      ${createAgent()}

    ${section("integration")}
  """

  //TODO: Test error codes coming back from service

  def createAgent(): Result = {
    Async.async {
      val agentName = InternalId.uidGenerator.create(32)
      val password = "test"

      val authenticationId = Async.await(AgentAssist.createAgent(agentName, password))

      authenticationId must_== agentName.toLowerCase + "." + agentName.toLowerCase
    }.await(60)
  }
}
