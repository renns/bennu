package com.qoid.bennu.integration

import com.qoid.bennu.client._
import org.specs2.Specification
import org.specs2.execute.Result
import scala.async.Async

class AliasLoginSpec extends Specification {
  implicit val config = HttpClientConfig()

  def is = s2"""
    ${section("integration")}

    Alias Login should
      get alias name      ${getAliasName()}

    ${section("integration")}
  """

  def getAliasName(): Result = {
    TestAssist.channelClient1 { client =>
      Async.async {
        val aliasName = "Anonymous"
        val alias = Async.await(client.getRootAlias())

        alias.name must_== aliasName
      }
    }.await(60)
  }
}
