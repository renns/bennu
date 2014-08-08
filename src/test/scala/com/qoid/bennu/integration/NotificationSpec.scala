package com.qoid.bennu.integration

import com.qoid.bennu.client._
import org.specs2.Specification
import org.specs2.execute.Result
import scala.async.Async

class NotificationSpec extends Specification {
  implicit val config = HttpClientConfig()

  def is = s2"""
    ${section("integration")}

    Notification should
      consume notification    ${consumeNotification()}

    ${section("integration")}
  """

  //TODO: Test error codes coming back from service
  //TODO: Test sending to another agent
  //TODO: Test DoV

  def consumeNotification(): Result = {
    ClientAssist.channelClient1 { client =>
      Async.async {
        //TODO: need to be able to create a notification (through introduction or verification) first
        todo
      }
    }.await(60)
  }
}
