package com.qoid.bennu.integration

import com.qoid.bennu.client._
import com.qoid.bennu.model.{Profile, Connection}
import org.specs2.Specification
import org.specs2.execute.Result
import scala.async.Async

class DegreesOfVisibilitySpec extends Specification {
  implicit val config = HttpClientConfig()

  def is = s2"""
    ${section("integration")}

    Degrees of Visibility should
      query profile 2 degrees         ${queryProfile()}

    ${section("integration")}
  """

  def queryProfile(): Result = {
    ClientAssist.anonymousClient1 { client =>
      Async.async {
        val introducerConnection = Async.await(client.getIntroducerConnection())
        val connections = Async.await(client.query[Connection](route = List(introducerConnection.iid)))
        val profiles = Async.await(client.query[Profile](route = List(introducerConnection.iid, connections.head.iid)))

        profiles.size must_== 1
      }
    }.await(60)
  }
}

