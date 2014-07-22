//package com.qoid.bennu.integration
//
//import com.qoid.bennu.client._
//import com.qoid.bennu.model.Profile
//import com.qoid.bennu.query.StandingQueryAction
//import org.specs2.Specification
//import org.specs2.execute.Result
//import scala.async.Async
//
//class RemoteProfileSpec extends Specification {
//  implicit val config = HttpClientConfig()
//
//  def is = s2"""
//    ${section("integration")}
//
//    Remote profile should
//      query historical      ${queryHistorical()}
//      query standing        ${queryStanding()}
//
//    ${section("integration")}
//  """
//
//  def queryHistorical(): Result = {
//    TestAssist.channelClient2 { (client1, client2) =>
//      Async.async {
//        val fAutoAccept1 = client1.autoAcceptIntroductions()
//        val fAutoAccept2 = client2.autoAcceptIntroductions()
//        Async.await(fAutoAccept1)
//        Async.await(fAutoAccept2)
//
//        val (conn12, _) = Async.await(TestAssist.introduce(client1, client2))
//        val profile2 = Async.await(client2.getProfile(client2.rootAliasIid))
//        val results = Async.await(client1.queryRemote[Profile]("", List(conn12.iid)))
//
//        (results.size must_== 1) and compareProfiles(results.head, profile2)
//      }
//    }.await(60)
//  }
//
//  def queryStanding(): Result = {
//    TestAssist.channelClient2 { (client1, client2) =>
//      Async.async {
//        val fAutoAccept1 = client1.autoAcceptIntroductions()
//        val fAutoAccept2 = client2.autoAcceptIntroductions()
//        Async.await(fAutoAccept1)
//        Async.await(fAutoAccept2)
//        val (conn12, _) = Async.await(TestAssist.introduce(client1, client2))
//
//        val profile2 = Async.await(client2.getProfile(client2.rootAliasIid))
//        val fResult = TestAssist.getStandingQueryResult[Profile](client1, StandingQueryAction.Update, "", None, List(conn12.iid))
//        Async.await(client2.upsert(profile2))
//        val result = Async.await(fResult)
//
//        compareProfiles(result, profile2)
//      }
//    }.await(60)
//  }
//
//  private def compareProfiles(p1: Profile, p2: Profile): Result = {
//    (p1.name must_== p2.name) and (p1.imgSrc must_== p2.imgSrc) and (p1.sharedId must_== p2.sharedId)
//  }
//}
