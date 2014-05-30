package com.qoid.bennu.integration

import com.qoid.bennu.JsonAssist.jsondsl._
import com.qoid.bennu.client._
import com.qoid.bennu.model._
import com.qoid.bennu.squery.StandingQueryAction
import org.specs2.Specification
import org.specs2.execute.Result
import scala.async.Async

class QuerySpec extends Specification {
  implicit val config = HttpClientConfig()

  def is = s2"""
    ${section("integration")}

    Query should
      query local historical              ${queryLocalHistorical()}
      query local standing                ${queryLocalStanding()}
      query sub-alias local historical    ${querySubAliasLocalHistorical()}
      query sub-alias local standing      ${querySubAliasLocalStanding()}
      query remote historical             ${queryRemoteHistorical()}
      query remote standing               ${queryRemoteStanding()}
      query remote meta-label historical  ${queryRemoteMetaLabelHistorical()}
      query remote connections historical ${queryRemoteConnectionsHistorical()}

    ${section("integration")}
  """

  def queryLocalHistorical(): Result = {
    TestAssist.channelClient1 { client =>
      Async.async {
        val alias = Async.await(client.getRootAlias())
        val label = Async.await(client.createLabel(alias.rootLabelIid, "A"))
        val results = Async.await(client.queryLocal[Label]("name = 'A'"))

        (results.size must_== 1) and (results.head.name must_== label.name)
      }
    }.await(30)
  }

  def queryLocalStanding(): Result = {
    TestAssist.channelClient1 { client =>
      Async.async {
        val alias = Async.await(client.getRootAlias())
        val label = Async.await(client.createLabel(alias.rootLabelIid, "A"))
        val fResult = TestAssist.getStandingQueryResult[Label](client, StandingQueryAction.Update, "name = 'A'")
        client.upsert(label)
        val result = Async.await(fResult)

        result.name must_== label.name
      }
    }.await(30)
  }

  def querySubAliasLocalHistorical(): Result = {
    TestAssist.channelClient1 { client =>
      Async.async {
        val alias = Async.await(client.getRootAlias())
        val subAlias = Async.await(client.createAlias(alias.rootLabelIid, "Sub-Alias"))
        val label = Async.await(client.createLabel(subAlias.rootLabelIid, "A"))
        val results = Async.await(client.queryLocal[Label]("name = 'A'", Some(subAlias.iid)))

        (results.size must_== 1) and (results.head.name must_== label.name)
      }
    }.await(30)
  }

  def querySubAliasLocalStanding(): Result = {
    TestAssist.channelClient1 { client =>
      Async.async {
        val alias = Async.await(client.getRootAlias())
        val subAlias = Async.await(client.createAlias(alias.rootLabelIid, "Sub-Alias"))
        val label = Async.await(client.createLabel(subAlias.rootLabelIid, "A"))
        val fResult = TestAssist.getStandingQueryResult[Label](client, StandingQueryAction.Update, "name = 'A'", Some(subAlias.iid))
        client.upsert(label)
        val result = Async.await(fResult)

        result.name must_== label.name
      }
    }.await(30)
  }

  def queryRemoteHistorical(): Result = {
    TestAssist.channelClient2 { (client1, client2) =>
      Async.async {
        val fAutoAccept1 = client1.autoAcceptIntroductions()
        val fAutoAccept2 = client2.autoAcceptIntroductions()
        Async.await(fAutoAccept1)
        Async.await(fAutoAccept2)
        val (conn12, conn21) = Async.await(TestAssist.introduce(client1, client2))

        val content = Async.await(TestAssist.createSampleContent(client2, "A", Some(conn21.iid)))
        val results = Async.await(client1.queryRemote[Content]("hasLabelPath('A')", List(conn12.iid)))

        (results.size must_== 1) and (results.head.data must_== content.data)
      }
    }.await(30)
  }

  def queryRemoteStanding(): Result = {
    TestAssist.channelClient2 { (client1, client2) =>
      Async.async {
        val fAutoAccept1 = client1.autoAcceptIntroductions()
        val fAutoAccept2 = client2.autoAcceptIntroductions()
        Async.await(fAutoAccept1)
        Async.await(fAutoAccept2)
        val (conn12, conn21) = Async.await(TestAssist.introduce(client1, client2))

        val fResult = TestAssist.getStandingQueryResult[Content](client1, StandingQueryAction.Insert, "hasLabelPath('A')", None, List(conn12.iid))
        val content = Async.await(TestAssist.createSampleContent(client2, "A", Some(conn21.iid)))
        val result = Async.await(fResult)

        result.data must_== content.data
      }
    }.await(30)
  }

  def queryRemoteMetaLabelHistorical(): Result = {
    TestAssist.channelClient2 { (client1, client2) =>
      Async.async {
        val fAutoAccept1 = client1.autoAcceptIntroductions()
        val fAutoAccept2 = client2.autoAcceptIntroductions()
        Async.await(fAutoAccept1)
        Async.await(fAutoAccept2)
        val (conn12, conn21) = Async.await(TestAssist.introduce(client1, client2))

        val alias = Async.await(client2.getRootAlias())
        val content = Async.await(client2.createContent(alias.iid, "TEXT", "text" -> "A", List(conn21.metaLabelIid)))
        val results = Async.await(client1.queryRemote[Content]("hasConnectionMetaLabel()", List(conn12.iid)))

        (results.size must_== 1) and (results.head.data must_== content.data)
      }
    }.await(30)
  }

  def queryRemoteConnectionsHistorical(): Result = {
    TestAssist.channelClient2 { (client1, client2) =>
      Async.async {
        val fAutoAccept1 = client1.autoAcceptIntroductions()
        val fAutoAccept2 = client2.autoAcceptIntroductions()
        Async.await(fAutoAccept1)
        Async.await(fAutoAccept2)
        val (conn12, _) = Async.await(TestAssist.introduce(client1, client2))

        val results = Async.await(client1.queryRemote[Connection]("", List(conn12.iid)))

        results.size must_== 2
      }
    }.await(30)
  }
}
