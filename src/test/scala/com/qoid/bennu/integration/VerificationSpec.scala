package com.qoid.bennu.integration

import com.qoid.bennu.JsonAssist.jsondsl._
import com.qoid.bennu.client._
import com.qoid.bennu.model._
import com.qoid.bennu.squery.StandingQueryAction
import m3.jdbc._
import org.specs2.Specification
import org.specs2.execute.Result
import org.specs2.execute.{Failure => Specs2Failure }
import scala.async.Async

class VerificationSpec extends Specification {
  implicit val config = HttpClientConfig()

  def is = s2"""
    ${section("integration")}

    Verification should
      request verification  ${requestVerification()}
      verify                ${verify()}

    ${section("integration")}
  """

  def requestVerification(): Result = {
    TestAssist.channelClient3 { (clientC, clientV, clientR) =>
      Async.async[Result] {
        val fAutoAcceptC = clientC.autoAcceptIntroductions()
        val fAutoAcceptV = clientV.autoAcceptIntroductions()
        val fAutoAcceptR = clientR.autoAcceptIntroductions()
        Async.await(fAutoAcceptC)
        Async.await(fAutoAcceptV)
        Async.await(fAutoAcceptR)
        val (connCV, _) = Async.await(TestAssist.introduce(clientC, clientV))
        val (connCR, connRC) = Async.await(TestAssist.introduce(clientC, clientR))
        val (_, connRV) = Async.await(TestAssist.introduce(clientV, clientR))

        val aliasC = Async.await(clientC.getRootAlias())
        val labelC = Async.await(clientC.createLabel(aliasC.rootLabelIid, "Claims"))
        Async.await(clientC.grantAccess(connCV.iid, labelC.iid))
        Async.await(clientC.grantAccess(connCR.iid, labelC.iid))
        val content = Async.await(clientC.createContent(aliasC.iid, "TEXT", "text" -> "This test will pass.", List(labelC.iid)))

        val fVerificationRequestNotification = TestAssist.getStandingQueryResult[Notification](clientV, StandingQueryAction.Insert)
        clientC.requestVerification(content, List(connCV), "Please verify")
        val verificationRequestNotification = Async.await(fVerificationRequestNotification)

        val fVerificationResponseNotification = TestAssist.getStandingQueryResult[Notification](clientC, StandingQueryAction.Insert)
        clientV.respondToVerification(verificationRequestNotification, "Claim verified")
        val verificationResponseNotification = Async.await(fVerificationResponseNotification)

        Async.await(clientC.acceptVerification(verificationResponseNotification))

        val claim = Async.await(clientR.queryRemote[Content]("hasLabelPath('Claims')", List(connRC.iid))).head
        val metaData = Content.MetaData.fromJson(claim.metaData)

        metaData.verifications match {
          case Some(verification :: Nil) =>
            val vContent = Async.await(clientR.queryRemote[Content](sql"iid = ${verification.verificationIid}", List(connRV.iid))).head
            val vMetaData = Content.MetaData.fromJson(vContent.metaData)

            vMetaData.verifiedContent match {
              case Some(verifiedContent) =>
                (verifiedContent.hash must_== claim.data) and (verification.hash must_== vContent.data)
              case _ => Specs2Failure("Verification meta-data incorrect")
            }
          case _ => Specs2Failure("Claim meta-data incorrect")
        }
      }
    }.await(60)
  }

  def verify(): Result = {
    TestAssist.channelClient3 { (clientC, clientV, clientR) =>
      Async.async[Result] {
        val fAutoAcceptC = clientC.autoAcceptIntroductions()
        val fAutoAcceptV = clientV.autoAcceptIntroductions()
        val fAutoAcceptR = clientR.autoAcceptIntroductions()
        Async.await(fAutoAcceptC)
        Async.await(fAutoAcceptV)
        Async.await(fAutoAcceptR)
        val (connCV, connVC) = Async.await(TestAssist.introduce(clientC, clientV))
        val (connCR, connRC) = Async.await(TestAssist.introduce(clientC, clientR))
        val (_, connRV) = Async.await(TestAssist.introduce(clientV, clientR))

        val aliasC = Async.await(clientC.getRootAlias())
        val labelC = Async.await(clientC.createLabel(aliasC.rootLabelIid, "Claims"))
        Async.await(clientC.grantAccess(connCV.iid, labelC.iid))
        Async.await(clientC.grantAccess(connCR.iid, labelC.iid))
        val content = Async.await(clientC.createContent(aliasC.iid, "TEXT", "text" -> "This test will pass.", List(labelC.iid)))

        val fVerificationResponseNotification = TestAssist.getStandingQueryResult[Notification](clientC, StandingQueryAction.Insert)
        clientV.verify(connVC, content, "Claim verified")
        val verificationResponseNotification = Async.await(fVerificationResponseNotification)

        Async.await(clientC.acceptVerification(verificationResponseNotification))

        val claim = Async.await(clientR.queryRemote[Content]("hasLabelPath('Claims')", List(connRC.iid))).head
        val metaData = Content.MetaData.fromJson(claim.metaData)

        metaData.verifications match {
          case Some(verification :: Nil) =>
            val vContent = Async.await(clientR.queryRemote[Content](sql"iid = ${verification.verificationIid}", List(connRV.iid))).head
            val vMetaData = Content.MetaData.fromJson(vContent.metaData)

            vMetaData.verifiedContent match {
              case Some(verifiedContent) =>
                (verifiedContent.hash must_== claim.data) and (verification.hash must_== vContent.data)
              case _ => Specs2Failure("Verification meta-data incorrect")
            }
          case _ => Specs2Failure("Claim meta-data incorrect")
        }
      }
    }.await(60)
  }
}
