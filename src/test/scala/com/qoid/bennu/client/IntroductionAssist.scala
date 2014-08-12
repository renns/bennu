package com.qoid.bennu.client

import com.qoid.bennu.model.Connection
import com.qoid.bennu.model.Notification
import com.qoid.bennu.model.id.InternalId
import com.qoid.bennu.query.StandingQueryAction
import m3.jdbc._
import scala.async.Async._
import scala.concurrent.Future
import scala.concurrent.Promise

trait IntroductionAssist {
  this: ChannelClient =>

  def autoAcceptIntroductions(): Future[Unit] = {
    async {
      val notifications = await(queryStanding[Notification](
        "kind = 'IntroductionRequest' and consumed = false"
      ) { (notification, action, context) =>
        if (action == StandingQueryAction.Insert) {
          acceptIntroduction(notification.iid)
        }
      })

      notifications.foreach(notification => acceptIntroduction(notification.iid))
    }
  }

  // Multiple introduce calls should not be run in parallel because it listens for any new
  // connections being created. If multiple are run in parallel, one instance may get the
  // new connection from the other instance.
  def introduce(
    aConnectionIid: InternalId,
    bConnectionIid: InternalId,
    clientA: ChannelClient,
    clientB: ChannelClient
  ): Future[(Connection, Connection)] = {
    async {
      val promiseA = Promise[Connection]()
      val promiseB = Promise[Connection]()

      clientA.queryStanding[Connection]() { (connection, action, context) =>
        if (action == StandingQueryAction.Insert) {
          clientA.cancelSubmit(context)
          promiseA.success(connection)
        }
      }

      clientB.queryStanding[Connection]() { (connection, action, context) =>
        if (action == StandingQueryAction.Insert) {
          clientB.cancelSubmit(context)
          promiseB.success(connection)
        }
      }

      await(initiateIntroduction(aConnectionIid, "Message to A", bConnectionIid, "Message to B"))

      (await(promiseA.future), await(promiseB.future))
    }
  }

  def connectThroughIntroducer(
    clientB: ChannelClient
  )(
    implicit config: HttpClientConfig
  ): Future[(Connection, Connection)] = {
    async {
      val fClientI = AgentAssist.login("Introducer", "introducer")
      val fConnAI = getIntroducerConnection()
      val fConnBI = clientB.getIntroducerConnection()

      val clientI = await(fClientI)
      val connAI = await(fConnAI)
      val connBI = await(fConnBI)

      val fConnIA = clientI.query[Connection](sql"localPeerId = ${connAI.remotePeerId} and remotePeerId = ${connAI.localPeerId}")
      val fConnIB = clientI.query[Connection](sql"localPeerId = ${connBI.remotePeerId} and remotePeerId = ${connBI.localPeerId}")

      val connIA = await(fConnIA).head
      val connIB = await(fConnIB).head

      await(clientI.introduce(connIA.iid, connIB.iid, this, clientB))
    }
  }
}
