//package com.qoid.bennu.integration
//
//import com.qoid.bennu.client._
//import com.qoid.bennu.model.Label
//import org.specs2.Specification
//import org.specs2.execute.Result
//import scala.async.Async
//
//class DeleteSpec extends Specification {
//  implicit val config = HttpClientConfig()
//
//  def is = s2"""
//    ${section("integration")}
//
//    Delete should
//      delete label      ${deleteLabel()}
//
//    ${section("integration")}
//  """
//
//  def deleteLabel(): Result = {
//    TestAssist.channelClient1 { client =>
//      Async.async {
//        val rootAlias = Async.await(client.getRootAlias())
//        val label = Async.await(client.createLabel(rootAlias.rootLabelIid, "Label"))
//        val deletedLabel = Async.await(client.delete[Label](label.iid))
//
//        deletedLabel.name must_== label.name
//      }
//    }.await(60)
//  }
//}
