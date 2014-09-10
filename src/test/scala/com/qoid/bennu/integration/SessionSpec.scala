package com.qoid.bennu.integration

import com.qoid.bennu.client._
import com.qoid.bennu.model.id.InternalId
import org.specs2.Specification
import org.specs2.execute.Result

import scala.async.Async

class SessionSpec extends Specification {
  implicit val config = HttpClientConfig()

  def is = s2"""
    ${section("integration")}

    Session should
      login and logout      ${loginLogout()}
      spawn session         ${spawnSession()}

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

  def spawnSession(): Result = {
    ClientAssist.channelClient1 { client1 =>
      Async.async {
        val alias1 = Async.await(client1.getAlias("Anonymous"))

        val alias2 = Async.await {
          client1.spawnSession(alias1.iid) { client2 =>
            Async.async {
              client2.alias
            }
          }
        }

        alias2.iid must_== alias1.iid
      }
    }.await(60)
  }
}
