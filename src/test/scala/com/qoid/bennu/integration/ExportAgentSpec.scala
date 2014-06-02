package com.qoid.bennu.integration

import com.qoid.bennu.client._
import com.qoid.bennu.model._
import com.qoid.bennu.model.id.InternalId
import com.qoid.bennu.model.introduction.IntroductionState
import com.qoid.bennu.model.notification.NotificationKind
import org.specs2.Specification
import org.specs2.execute.Result
import org.specs2.execute.{Failure => Specs2Failure }
import scala.async.Async
import scala.concurrent._
import scala.util._

class ExportAgentSpec extends Specification {
  implicit val config = HttpClientConfig()

  def is = s2"""
    ${section("integration")}

    Export agent should
      export and import agent      ${exportThenImport()}

    ${section("integration")}
  """

  def exportThenImport(): Result = {
    Async.async[Result] {
      val client1 = Async.await(HttpAssist.createAgent())
      Async.await(TestAssist.createSampleContent(client1, "A", Some(InternalId.random)))
      Async.await(client1.upsert(Introduction(InternalId.random, IntroductionState.NotResponded, InternalId.random, IntroductionState.NotResponded)))
      Async.await(client1.upsert(Notification(InternalId.random, NotificationKind.IntroductionRequest)))
      client1.close()

      val client2 = Async.await(ChannelClientFactory.createHttpChannelClient(client1.agentName))
      val agentData = Async.await(client2.deleteAgent(true))
      client2.close()

      Async.await(canLogin(client1.agentName)) match {
        case true =>
          Specs2Failure("Able to login after deleting agent")
        case false =>
          Async.await(HttpAssist.importAgent(agentData))

          val client3 = Async.await(ChannelClientFactory.createHttpChannelClient(client1.agentName))
          val agentData2 = Async.await(client3.deleteAgent(true))

          agentData2 must_== agentData
      }
    }.await(60)
  }

  private def canLogin(agentName: String): Future[Boolean] = {
    val p = Promise[Boolean]()

    ChannelClientFactory.createHttpChannelClient(agentName).onComplete {
      case Success(_) => p.success(true)
      case Failure(_) => p.success(false)
    }

    p.future
  }
}
