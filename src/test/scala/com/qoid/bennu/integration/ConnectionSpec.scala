package com.qoid.bennu.integration

import com.qoid.bennu.client._
import org.specs2.Specification
import org.specs2.execute.Result
import scala.async.Async

class ConnectionSpec extends Specification {
  implicit val config = HttpClientConfig()

  def is = s2"""
    ${section("integration")}

    Connection should
      delete connection     ${deleteConnection()}

    ${section("integration")}
  """

  //TODO: Test error codes coming back from service
  //TODO: Test sending to another agent
  //TODO: Test DoV

  def deleteConnection(): Result = {
    ClientAssist.channelClient1 { client =>
      Async.async {
        val connections = Async.await(client.getConnections())
        val connection = connections.find(_.iid != client.alias.connectionIid).head
        val connectionIid = Async.await(client.deleteConnection(connection.iid))
        connectionIid must_== connection.iid
      }
    }.await(60)
  }
}
