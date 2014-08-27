package com.qoid.bennu.integration

import com.qoid.bennu.JsonAssist._
import com.qoid.bennu.JsonAssist.jsondsl._
import com.qoid.bennu.client._
import com.qoid.bennu.model.Notification
import m3.jdbc._
import org.specs2.Specification
import org.specs2.execute.Result
import scala.async.Async

class NotificationSpec extends Specification {
  implicit val config = HttpClientConfig()

  def is = s2"""
    ${section("integration")}

    Notification should
      create notification     ${createNotification()}
      consume notification    ${consumeNotification()}
      delete notification     ${deleteNotification()}

    ${section("integration")}
  """

  //TODO: Test error codes coming back from service
  //TODO: Test DoV

  def createNotification(): Result = {
    ClientAssist.anonymousClient2 { (client1, client2) =>
      Async.async {
        val fAutoAccept1 = client1.autoAcceptIntroductions()
        val fAutoAccept2 = client2.autoAcceptIntroductions()
        Async.await(fAutoAccept1)
        Async.await(fAutoAccept2)

        val (conn12, conn21) = Async.await(client1.connectThroughIntroducer(client2))

        val kind = "Test"
        val data: JValue = "test" -> "test"
        val route: JValue = List(conn21.iid)

        val notification1 = Async.await(client1.createNotification(kind, data, List(conn12.iid)))
        val notification2 = Async.await(client2.query[Notification](sql"kind = ${kind}")).head

        (notification1.kind must_== kind) and
          (notification1.data must_== data) and
          (notification1.consumed must_== false) and
          (notification2.iid must_== notification1.iid) and
          (notification2.kind must_== kind) and
          (notification2.data must_== data) and
          (notification2.consumed must_== false) and
          (notification2.route must_== route)
      }
    }.await(60)
  }

  def consumeNotification(): Result = {
    ClientAssist.channelClient1 { client =>
      Async.async {
        val notification = Async.await(client.createNotification("Test", "test" -> "test"))
        val notificationIid = Async.await(client.consumeNotification(notification.iid))
        notificationIid must_== notification.iid
      }
    }.await(60)
  }

  def deleteNotification(): Result = {
    ClientAssist.channelClient1 { client =>
      Async.async {
        val notification = Async.await(client.createNotification("Test", "test" -> "test"))
        val notificationIid = Async.await(client.deleteNotification(notification.iid))
        notificationIid must_== notification.iid
      }
    }.await(60)
  }
}
