package com.qoid.bennu.integration

import com.qoid.bennu.JsonAssist._
import com.qoid.bennu.JsonAssist.jsondsl._
import com.qoid.bennu.ServicePath
import com.qoid.bennu.client._
import com.qoid.bennu.model.id.InternalId
import m3.predef._
import org.specs2.Specification
import org.specs2.execute.Result

import scala.async.Async

class CreateAgentSpec extends Specification {
  implicit val config = HttpClientConfig()

  def is = s2"""
    ${section("integration")}

    Create Agent should
      create agent      ${createAgent()}

    ${section("integration")}
  """

  //TODO: Test error codes coming back from service

  def createAgent(): Result = {
    Async.async {
      val agentName = InternalId.uidGenerator.create(32)
      val password = "test"
      val httpAssist = new HttpAssist with Logging {}

      val createAgentBody = ("name" -> agentName) ~ ("password" -> password)
      val response = Async.await(httpAssist.httpPost(s"${config.server}${ServicePath.createAgent}", createAgentBody, None))

      val expected: JValue = "authenticationId" -> s"${agentName.toLowerCase}.${agentName.toLowerCase}"

      parseJson(response) must_== expected
    }.await(60)
  }
}
