package com.qoid.bennu.integration

import com.qoid.bennu.client._
import com.qoid.bennu.model.Content
import com.qoid.bennu.squery.StandingQueryAction
import org.specs2._
import org.specs2.execute.Result
import scala.async.Async

class DegreesOfVisibilitySpec extends Specification {
  implicit val config = HttpClientConfig()

  def is = s2"""
    ${section("integration")}

    Degrees of Visibility should
      query 2 degrees historical      ${degrees2Historical()}
      query 2 degrees standing        ${degrees2Standing()}
      query 3 degrees historical      ${degrees3Historical()}
      query 3 degrees standing        ${degrees3Standing()}

    ${section("integration")}
  """

  def degrees2Historical(): Result = {
    TestAssist.channelClient3 { (client1, client2, client3) =>
      Async.async {
        val fAutoAccept1 = client1.autoAcceptIntroductions()
        val fAutoAccept2 = client2.autoAcceptIntroductions()
        val fAutoAccept3 = client3.autoAcceptIntroductions()
        Async.await(fAutoAccept1)
        Async.await(fAutoAccept2)
        Async.await(fAutoAccept3)

        val (conn12, _) = Async.await(TestAssist.introduce(client1, client2))
        val (conn23, conn32) = Async.await(TestAssist.introduce(client2, client3))

        Async.await(client3.updateConnection(conn32.iid, 2))
        val content = Async.await(TestAssist.createSampleContent(client3, "A", Some(conn32.iid)))
        val results = Async.await(client1.queryRemote[Content]("hasLabelPath('A')", List(conn12.iid, conn23.iid)))

        (results.size must_== 1) and (results.head.data must_== content.data)
      }
    }.await(60)
  }

  def degrees2Standing(): Result = {
    TestAssist.channelClient3 { (client1, client2, client3) =>
      Async.async {
        val fAutoAccept1 = client1.autoAcceptIntroductions()
        val fAutoAccept2 = client2.autoAcceptIntroductions()
        val fAutoAccept3 = client3.autoAcceptIntroductions()
        Async.await(fAutoAccept1)
        Async.await(fAutoAccept2)
        Async.await(fAutoAccept3)

        val (conn12, _) = Async.await(TestAssist.introduce(client1, client2))
        val (conn23, conn32) = Async.await(TestAssist.introduce(client2, client3))

        Async.await(client3.updateConnection(conn32.iid, 2))
        val fResult = TestAssist.getStandingQueryResult[Content](client1, StandingQueryAction.Insert, "hasLabelPath('A')", None, List(conn12.iid, conn23.iid))
        val content = Async.await(TestAssist.createSampleContent(client3, "A", Some(conn32.iid)))
        val result = Async.await(fResult)

        result.data must_== content.data
      }
    }.await(60)
  }

  def degrees3Historical(): Result = {
    TestAssist.channelClient4 { (client1, client2, client3, client4) =>
      Async.async {
        val fAutoAccept1 = client1.autoAcceptIntroductions()
        val fAutoAccept2 = client2.autoAcceptIntroductions()
        val fAutoAccept3 = client3.autoAcceptIntroductions()
        val fAutoAccept4 = client4.autoAcceptIntroductions()
        Async.await(fAutoAccept1)
        Async.await(fAutoAccept2)
        Async.await(fAutoAccept3)
        Async.await(fAutoAccept4)

        val (conn12, _) = Async.await(TestAssist.introduce(client1, client2))
        val (conn23, _) = Async.await(TestAssist.introduce(client2, client3))
        val (conn34, conn43) = Async.await(TestAssist.introduce(client3, client4))

        Async.await(client4.updateConnection(conn43.iid, 3))
        val content = Async.await(TestAssist.createSampleContent(client4, "A", Some(conn43.iid)))
        val results = Async.await(client1.queryRemote[Content]("hasLabelPath('A')", List(conn12.iid, conn23.iid, conn34.iid)))

        (results.size must_== 1) and (results.head.data must_== content.data)
      }
    }.await(60)
  }

  def degrees3Standing(): Result = {
    TestAssist.channelClient4 { (client1, client2, client3, client4) =>
      Async.async {
        val fAutoAccept1 = client1.autoAcceptIntroductions()
        val fAutoAccept2 = client2.autoAcceptIntroductions()
        val fAutoAccept3 = client3.autoAcceptIntroductions()
        val fAutoAccept4 = client4.autoAcceptIntroductions()
        Async.await(fAutoAccept1)
        Async.await(fAutoAccept2)
        Async.await(fAutoAccept3)
        Async.await(fAutoAccept4)

        val (conn12, _) = Async.await(TestAssist.introduce(client1, client2))
        val (conn23, _) = Async.await(TestAssist.introduce(client2, client3))
        val (conn34, conn43) = Async.await(TestAssist.introduce(client3, client4))

        Async.await(client4.updateConnection(conn43.iid, 3))
        val fResult = TestAssist.getStandingQueryResult[Content](client1, StandingQueryAction.Insert, "hasLabelPath('A')", None, List(conn12.iid, conn23.iid, conn34.iid))
        val content = Async.await(TestAssist.createSampleContent(client4, "A", Some(conn43.iid)))
        val result = Async.await(fResult)

        result.data must_== content.data
      }
    }.await(60)
  }
}
