package com.qoid.bennu.integration

import com.qoid.bennu.client._
import com.qoid.bennu.model.Label
import com.qoid.bennu.query.StandingQueryAction
import m3.jdbc._
import org.specs2.Specification
import org.specs2.execute.Result
import scala.async.Async
import scala.concurrent._
import scala.concurrent.duration.Duration

class QuerySpec extends Specification {
  implicit val config = HttpClientConfig()

  def is = s2"""
    ${section("integration")}

    Query should
      query standing        ${queryStanding()}
      cancel standing       ${cancelStanding()}

    ${section("integration")}
  """
  //      query historical              ${queryLocalHistorical()}
  //      query standing                ${queryLocalStanding()}
  //      query sub-alias historical    ${querySubAliasLocalHistorical()}
  //      query sub-alias standing      ${querySubAliasLocalStanding()}
  //      query meta-label historical  ${queryRemoteMetaLabelHistorical()}
  //      query connections historical ${queryRemoteConnectionsHistorical()}

  def queryStanding(): Result = {
    ClientAssist.channelClient1 { client =>
      Async.async {
        val p = Promise[Label]()
        val alias = Async.await(client.getCurrentAlias())
        val labelName = "Label"

        Async.await(client.queryStanding[Label](sql"name = ${labelName}") { (label, action, context) =>
          if (action == StandingQueryAction.Insert) {
            client.cancelSubmit(context)
            p.success(label)
          }
        })

        client.createLabel(alias.labelIid, labelName)

        val l = Async.await(p.future)

        l.name must_== labelName
      }
    }.await(60)
  }

  def cancelStanding(): Result = {
    ClientAssist.channelClient1 { client =>
      Async.async {
        val p = Promise[Unit]()
        var queryCancelled = false
        val alias = Async.await(client.getCurrentAlias())
        val label = Async.await(client.createLabel(alias.labelIid, "Label"))

        Async.await(client.queryStanding[Label](sql"iid = ${label.iid}") { (label, action, context) =>
          if (action == StandingQueryAction.Update && !queryCancelled) {
            Async.async {
              Async.await(client.cancelQuery(context))
              queryCancelled = true
              client.updateLabel(label.iid, "Label3")
            }
          } else {
            p.success()
          }
        })

        client.updateLabel(label.iid, "Label2")

        try {
          Await.result(p.future, Duration("5s"))
          failure
        } catch {
          case e: TimeoutException => success
        }
      }
    }.await(60)
  }
}

//
//  def queryLocalHistorical(): Result = {
//    TestAssist.channelClient1 { client =>
//      Async.async {
//        val alias = Async.await(client.getRootAlias())
//        val label = Async.await(client.createLabel(alias.rootLabelIid, "A"))
//        val results = Async.await(client.queryLocal[Label]("name = 'A'"))
//
//        (results.size must_== 1) and (results.head.name must_== label.name)
//      }
//    }.await(60)
//  }
//
//  def queryLocalStanding(): Result = {
//    TestAssist.channelClient1 { client =>
//      Async.async {
//        val alias = Async.await(client.getRootAlias())
//        val label = Async.await(client.createLabel(alias.rootLabelIid, "A"))
//        val fResult = TestAssist.getStandingQueryResult[Label](client, StandingQueryAction.Update, "name = 'A'")
//        client.upsert(label)
//        val result = Async.await(fResult)
//
//        result.name must_== label.name
//      }
//    }.await(60)
//  }
//
//  def querySubAliasLocalHistorical(): Result = {
//    TestAssist.channelClient1 { client =>
//      Async.async {
//        val alias = Async.await(client.getRootAlias())
//        val subAlias = Async.await(client.createAlias(alias.rootLabelIid, "Sub-Alias"))
//        val label = Async.await(client.createLabel(subAlias.rootLabelIid, "A"))
//        val results = Async.await(client.queryLocal[Label]("name = 'A'", Some(subAlias.iid)))
//
//        (results.size must_== 1) and (results.head.name must_== label.name)
//      }
//    }.await(60)
//  }
//
//  def querySubAliasLocalStanding(): Result = {
//    TestAssist.channelClient1 { client =>
//      Async.async {
//        val alias = Async.await(client.getRootAlias())
//        val subAlias = Async.await(client.createAlias(alias.rootLabelIid, "Sub-Alias"))
//        val label = Async.await(client.createLabel(subAlias.rootLabelIid, "A"))
//        val fResult = TestAssist.getStandingQueryResult[Label](client, StandingQueryAction.Update, "name = 'A'", Some(subAlias.iid))
//        client.upsert(label)
//        val result = Async.await(fResult)
//
//        result.name must_== label.name
//      }
//    }.await(60)
//  }
//
//  def queryRemoteHistorical(): Result = {
//    TestAssist.channelClient2 { (client1, client2) =>
//      Async.async {
//        val fAutoAccept1 = client1.autoAcceptIntroductions()
//        val fAutoAccept2 = client2.autoAcceptIntroductions()
//        Async.await(fAutoAccept1)
//        Async.await(fAutoAccept2)
//        val (conn12, conn21) = Async.await(TestAssist.introduce(client1, client2))
//
//        val content = Async.await(TestAssist.createSampleContent(client2, "A", Some(conn21.iid)))
//        val results = Async.await(client1.queryRemote[Content]("hasLabelPath('A')", List(conn12.iid)))
//
//        (results.size must_== 1) and (results.head.data must_== content.data)
//      }
//    }.await(60)
//  }
//
//  def queryRemoteStanding(): Result = {
//    TestAssist.channelClient2 { (client1, client2) =>
//      Async.async {
//        val fAutoAccept1 = client1.autoAcceptIntroductions()
//        val fAutoAccept2 = client2.autoAcceptIntroductions()
//        Async.await(fAutoAccept1)
//        Async.await(fAutoAccept2)
//        val (conn12, conn21) = Async.await(TestAssist.introduce(client1, client2))
//
//        val fResult = TestAssist.getStandingQueryResult[Content](client1, StandingQueryAction.Insert, "hasLabelPath('A')", None, List(conn12.iid))
//        val content = Async.await(TestAssist.createSampleContent(client2, "A", Some(conn21.iid)))
//        val result = Async.await(fResult)
//
//        result.data must_== content.data
//      }
//    }.await(60)
//  }
//
//  def queryRemoteMetaLabelHistorical(): Result = {
//    TestAssist.channelClient2 { (client1, client2) =>
//      Async.async {
//        val fAutoAccept1 = client1.autoAcceptIntroductions()
//        val fAutoAccept2 = client2.autoAcceptIntroductions()
//        Async.await(fAutoAccept1)
//        Async.await(fAutoAccept2)
//        val (conn12, conn21) = Async.await(TestAssist.introduce(client1, client2))
//
//        val content = Async.await(client2.createContent("TEXT", "text" -> "A", List(conn21.metaLabelIid)))
//        val results = Async.await(client1.queryRemote[Content]("hasConnectionMetaLabel()", List(conn12.iid)))
//
//        (results.size must_== 1) and (results.head.data must_== content.data)
//      }
//    }.await(60)
//  }
//
//  def queryRemoteConnectionsHistorical(): Result = {
//    TestAssist.channelClient2 { (client1, client2) =>
//      Async.async {
//        val fAutoAccept1 = client1.autoAcceptIntroductions()
//        val fAutoAccept2 = client2.autoAcceptIntroductions()
//        Async.await(fAutoAccept1)
//        Async.await(fAutoAccept2)
//        val (conn12, _) = Async.await(TestAssist.introduce(client1, client2))
//
//        val results = Async.await(client1.queryRemote[Connection]("", List(conn12.iid)))
//
//        results.size must_== 4
//      }
//    }.await(60)
//  }
//}
