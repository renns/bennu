package com.qoid.bennu.integration

import com.qoid.bennu.client._
import com.qoid.bennu.model.id.InternalId
import org.specs2.Specification
import org.specs2.execute.Result

import scala.async.Async

class LoginSpec extends Specification {
  implicit val config = HttpClientConfig()

  def is = s2"""
    ${section("integration")}

    Login should
      login and logout      ${loginLogout()}

    ${section("integration")}
  """

  //TODO: Test error codes coming back from service

  def loginLogout(): Result = {
    Async.async {
      val agentName = InternalId.uidGenerator.create(32)
      val password = "test"

      val authenticationId = Async.await(AgentAssist.createAgent(agentName, password))
      val client = Async.await(AgentAssist.login(authenticationId, password))
      client.close()
      Async.await(client.logout())

      success
    }.await(60)
  }
}
