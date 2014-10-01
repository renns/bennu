package com.qoid.bennu.integration

import com.qoid.bennu.client._
import org.specs2.Specification
import org.specs2.execute.Result
import scala.async.Async

class IntroductionSpec extends Specification {
  implicit val config = HttpClientConfig()

  def is = s2"""
    ${section("integration")}

    Introduction should
      accept introduction      ${acceptIntroduction()}

    ${section("integration")}
  """

  def acceptIntroduction(): Result = {
    ClientAssist.channelClient2 { (client1, client2) =>
      Async.async {
        val fAutoAccept1 = client1.autoAcceptIntroductions()
        val fAutoAccept2 = client2.autoAcceptIntroductions()
        Async.await(fAutoAccept1)
        Async.await(fAutoAccept2)

        val (conn12, conn21) = Async.await(client1.connectThroughIntroducer(client2))

        (conn12.localPeerId must_== conn21.remotePeerId) and (conn12.remotePeerId must_== conn21.localPeerId)
      }
    }.await(60)
  }
}
