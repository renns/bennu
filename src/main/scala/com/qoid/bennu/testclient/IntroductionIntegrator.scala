package com.qoid.bennu.testclient

import com.qoid.bennu.JdbcAssist
import com.qoid.bennu.JsonAssist._
import com.qoid.bennu.model._
import com.qoid.bennu.squery.StandingQueryAction
import com.qoid.bennu.testclient.client.HttpAssist.HttpClientConfig
import com.qoid.bennu.testclient.client._
import com.qoid.bennu.webservices.QueryService
import m3.guice.GuiceApp
import m3.predef._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future, Promise}
import scala.util.Failure
import scala.util.Success
import scala.concurrent.duration.Duration

object IntroductionIntegrator extends GuiceApp {
  val results = run()

  println("\nResults:")

  results.foreach {
    case (name, None) => println(s"  $name -- PASS")
    case (name, Some(e)) => println(s"  $name -- FAIL\n${e.getMessage}\n${e.getStackTraceString}")
  }

  System.exit(0)

  def run(): List[(String, Option[Exception])] = {
    implicit val config = HttpAssist.HttpClientConfig()

    List[(String, () => Option[Exception])](
      ("Introduction - Accept/Accept Standing Query", acceptAcceptStandingQuery)
    ).map { t =>
      logger.debug(s"Test started -- ${t._1}")
      val result = t._2()
      logger.debug(s"Test ended -- ${t._1} -- ${if (result.isEmpty) "PASS" else "FAIL"}")
      (t._1, result)
    }
  }

//  def run(aAccept: Boolean, bAccept: Boolean): Unit = {
//    val testName = "Introduction (" + (if (aAccept) "accept" else "reject") + "/" + (if (bAccept) "accept" else "reject") + ")"
//
//    try {
//      val clientA = HttpAssist.createAgent("A")
//      val clientB = HttpAssist.createAgent("B")
//      val clientC = HttpAssist.createAgent("C")
//      val aliasA = clientA.getRootAlias()
//      val aliasB = clientB.getRootAlias()
//      val aliasC = clientC.getRootAlias()
//      val connAIntro = clientA.query[Connection]("").head
//      val connBIntro = clientB.query[Connection]("").head
//      val (connAC, connCA) = TestAssist.createConnection(clientA, aliasA, clientC, aliasC)
//      val (connBC, connCB) = TestAssist.createConnection(clientB, aliasB, clientC, aliasC)
//
//      clientC.initiateIntroduction(connCA, "Message to A", connCB, "Message to B")
//
//      // Give C time to send notifications
//      Thread.sleep(1000)
//
//      val notificationA = clientA.query[Notification](s"consumed = false and fromConnectionIid = '${connAC.iid.value}' and kind = '${NotificationKind.IntroductionRequest}'").head
//      logger.debug(s"Received notification -- $notificationA")
//      clientA.respondToIntroduction(notificationA, aAccept)
//
//      val notificationB = clientB.query[Notification](s"consumed = false and fromConnectionIid = '${connBC.iid.value}' and kind = '${NotificationKind.IntroductionRequest}'").head
//      logger.debug(s"Received notification -- $notificationB")
//      clientB.respondToIntroduction(notificationB, bAccept)
//
//      // Give C time to create connections
//      Thread.sleep(1000)
//
//      val aConnections = clientA.query[Connection](sql"iid <> ${connAC.iid} and iid <> ${connAIntro.iid}")
//      val bConnections = clientB.query[Connection](sql"iid <> ${connBC.iid} and iid <> ${connBIntro.iid}")
//
//      if (aAccept && bAccept) {
//        if (aConnections.head.localPeerId == bConnections.head.remotePeerId && aConnections.head.remotePeerId == bConnections.head.localPeerId) {
//          logger.debug(s"$testName: PASS")
//        } else {
//          logger.warn(s"$testName: FAIL -- Created connections are not a pair")
//        }
//      } else {
//        if (aConnections.isEmpty && bConnections.isEmpty) {
//          logger.debug(s"$testName: PASS")
//        } else {
//          logger.warn(s"$testName: FAIL -- Connections created when introduction was rejected")
//        }
//      }
//    } catch {
//      case e: Exception => logger.warn(s"$testName: FAIL", e)
//    }
//  }

  def acceptAcceptStandingQuery()(implicit config: HttpClientConfig): Option[Exception] = {
    try {
      val pA = Promise[Connection]()
      val pB = Promise[Connection]()

      val clientA = HttpAssist.createAgent("A")
      val clientB = HttpAssist.createAgent("B")
      val clientC = HttpAssist.createAgent("C")
      val aliasA = clientA.getRootAlias()
      val aliasB = clientB.getRootAlias()
      val aliasC = clientC.getRootAlias()
      val (_, connCA) = TestAssist.createConnection(clientA, aliasA, clientC, aliasC)
      val (_, connCB) = TestAssist.createConnection(clientB, aliasB, clientC, aliasC)

      getSQueryResult[Notification](clientA).onComplete {
        case Success(n) =>
          getSQueryResult[Connection](clientA).onComplete {
            case Success(c) => pA.success(c)
            case Failure(e) => pA.failure(e)
          }

          clientA.respondToIntroduction(n, true)
        case Failure(e) => pA.failure(e)
      }

      getSQueryResult[Notification](clientB).onComplete {
        case Success(n) =>
          getSQueryResult[Connection](clientB).onComplete {
            case Success(c) => pB.success(c)
            case Failure(e) => pB.failure(e)
          }

          clientB.respondToIntroduction(n, true)
        case Failure(e) => pB.failure(e)
      }

      clientC.initiateIntroduction(connCA, "Message to A", connCB, "Message to B")

      val connAB = Await.result(pA.future, Duration("10 seconds"))
      val connBA = Await.result(pB.future, Duration("10 seconds"))

      if (connAB.localPeerId == connBA.remotePeerId && connAB.remotePeerId == connBA.localPeerId) {
        None
      } else {
        Some(new Exception("Created connections are not a pair"))
      }
    } catch {
      case e: Exception => Some(e)
    }
  }

  private def getSQueryResult[T <: HasInternalId : Manifest](client: ChannelClient): Future[T] = {
    val p = Promise[T]()

    try {
      client.query[T]("", historical = false, standing = true) { response =>
        val typeName = manifest[T].runtimeClass.getSimpleName

        QueryService.ResponseData.fromJson(response.data) match {
          case QueryService.ResponseData(_, _, tpe, Some(StandingQueryAction.Insert), JArray(i :: Nil)) if tpe =:= typeName =>
            val mapper = JdbcAssist.findMapperByTypeName(typeName)
            p.success(mapper.fromJson(i).asInstanceOf[T])
          case r => p.failure(new Exception(s"Unexpected response data -- $r"))
        }
      }
    } catch {
      case e: Exception => p.failure(e)
    }

    p.future
  }
}
