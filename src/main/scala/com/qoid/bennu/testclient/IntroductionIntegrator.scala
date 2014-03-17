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
import scala.concurrent.duration.Duration
import scala.concurrent._
import scala.util.Failure
import scala.util.Success

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
      ("Introduction - Accept", introduceAccept)
    ).map { t =>
      logger.debug(s"Test started -- ${t._1}")
      val result = t._2()
      logger.debug(s"Test ended -- ${t._1} -- ${if (result.isEmpty) "PASS" else "FAIL"}")
      (t._1, result)
    }
  }

  def introduceAccept()(implicit config: HttpClientConfig): Option[Exception] = {
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

          // Put a delay to prevent race condition. This can be removed once race condition is fixed.
          Thread.sleep(1000)
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
            client.deRegisterStandingQuery(response.handle)
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
