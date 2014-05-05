package com.qoid.bennu.testclient

import com.qoid.bennu.JdbcAssist
import com.qoid.bennu.JsonAssist.jsondsl._
import com.qoid.bennu.model._
import com.qoid.bennu.squery.StandingQueryAction
import com.qoid.bennu.testclient.client.HttpAssist.HttpClientConfig
import com.qoid.bennu.testclient.client._
import m3.guice.GuiceApp
import m3.jdbc._
import m3.predef._
import net.liftweb.json.JsonAST.JArray
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent._
import scala.concurrent.duration.Duration

object VerificationIntegrator extends GuiceApp {
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
      ("Verification - Request", requestVerification),
      ("Verification - Verify", verify)
    ).map { t =>
      logger.debug(s"Test started -- ${t._1}")
      val result = t._2()
      logger.debug(s"Test ended -- ${t._1} -- ${if (result.isEmpty) "PASS" else "FAIL"}")
      (t._1, result)
    }
  }

  def requestVerification()(implicit config: HttpClientConfig): Option[Exception] = {
    try {
      val p = Promise[Unit]()

      val clientC = HttpAssist.createAgent("Claimant")
      val clientV = HttpAssist.createAgent("Verifier")
      val clientR = HttpAssist.createAgent("Reliant")

      val aliasC = clientC.getRootAlias()
      val aliasV = clientV.getRootAlias()
      val aliasR = clientR.getRootAlias()

      val (connCV, _) = TestAssist.createConnection(clientC, aliasC.iid, clientV, aliasV.iid)
      val (connCR, connRC) = TestAssist.createConnection(clientC, aliasC.iid, clientR, aliasR.iid)
      val (_, connRV) = TestAssist.createConnection(clientV, aliasV.iid, clientR, aliasR.iid)

      val label = clientC.createLabel(aliasC.rootLabelIid, "Claims")
      clientC.grantAccess(connCV.iid, label.iid)
      clientC.grantAccess(connCR.iid, label.iid)
      val content = clientC.createContent(aliasC.iid, "TEXT", "text" -> "This test will pass.", List(label.iid))

      for (verificationRequestNotification <- getSQueryResult[Notification](clientV)) {
        for (verificationResponseNotification <- getSQueryResult[Notification](clientC)) {
          clientC.acceptVerification(verificationResponseNotification)

          for (claim <- getRemoteContent(clientR, connRC, sql"hasLabelPath('Claims')")) {
            val metaData = Content.MetaData.fromJson(claim.metaData)

            metaData.verifications match {
              case Some(verification :: Nil) =>
                for (vContent <- getRemoteContent(clientR, connRV, sql"iid = ${verification.verificationIid}")) {
                  val vMetaData = Content.MetaData.fromJson(vContent.metaData)

                  vMetaData.verifiedContent match {
                    case Some(verifiedContent) =>
                      if (verifiedContent.hash == claim.data && verification.hash == vContent.data) {
                        p.success()
                      } else {
                        p.failure(new Exception("Claim and/or verification hashes incorrect"))
                      }
                    case _ => p.failure(new Exception("Verification meta-data incorrect"))
                  }
                }
              case _ => p.failure(new Exception("Claim meta-data incorrect"))
            }
          }
        }

        clientV.respondToVerification(verificationRequestNotification, "Claim verified")
      }

      clientC.requestVerification(content, List(connCV), "Please verify")

      Await.result(p.future, Duration("20 seconds"))

      None
    } catch {
      case e: Exception => Some(e)
    }
  }

  def verify()(implicit config: HttpClientConfig): Option[Exception] = {
    try {
      val p = Promise[Unit]()

      val clientC = HttpAssist.createAgent("Claimant")
      val clientV = HttpAssist.createAgent("Verifier")
      val clientR = HttpAssist.createAgent("Reliant")

      val aliasC = clientC.getRootAlias()
      val aliasV = clientV.getRootAlias()
      val aliasR = clientR.getRootAlias()

      val (connCV, connVC) = TestAssist.createConnection(clientC, aliasC.iid, clientV, aliasV.iid)
      val (connCR, connRC) = TestAssist.createConnection(clientC, aliasC.iid, clientR, aliasR.iid)
      val (_, connRV) = TestAssist.createConnection(clientV, aliasV.iid, clientR, aliasR.iid)

      val label = clientC.createLabel(aliasC.rootLabelIid, "Claims")
      clientC.grantAccess(connCV.iid, label.iid)
      clientC.grantAccess(connCR.iid, label.iid)
      val content = clientC.createContent(aliasC.iid, "TEXT", "text" -> "This test will pass.", List(label.iid))

      for (verificationResponseNotification <- getSQueryResult[Notification](clientC)) {
        clientC.acceptVerification(verificationResponseNotification)

        for (claim <- getRemoteContent(clientR, connRC, sql"hasLabelPath('Claims')")) {
          val metaData = Content.MetaData.fromJson(claim.metaData)

          metaData.verifications match {
            case Some(verification :: Nil) =>
              for (vContent <- getRemoteContent(clientR, connRV, sql"iid = ${verification.verificationIid}")) {
                val vMetaData = Content.MetaData.fromJson(vContent.metaData)

                vMetaData.verifiedContent match {
                  case Some(verifiedContent) =>
                    if (verifiedContent.hash == claim.data && verification.hash == vContent.data) {
                      p.success()
                    } else {
                      p.failure(new Exception("Claim and/or verification hashes incorrect"))
                    }
                  case _ => p.failure(new Exception("Verification meta-data incorrect"))
                }
              }
            case _ => p.failure(new Exception("Claim meta-data incorrect"))
          }
        }
      }

      clientV.verify(connVC, content, "Claim verified")


      Await.result(p.future, Duration("20 seconds"))

      None
    } catch {
      case e: Exception => Some(e)
    }
  }

  private def getSQueryResult[T <: HasInternalId : Manifest](client: ChannelClient): Future[T] = {
    val p = Promise[T]()

    try {
      client.query[T]("", historical = false, standing = true) { response =>
        val mapper = JdbcAssist.findMapperByType[T]

        response match {
          case QueryResponse(
            QueryResponseType.SQuery,
            handle,
            tpe,
            _,
            JArray(i :: Nil),
            _,
            _,
            Some(StandingQueryAction.Insert)
          ) if tpe =:= mapper.typeName =>
            client.deRegisterStandingQuery(handle)
            p.success(mapper.fromJson(i))
          case r => p.failure(new Exception(s"Unexpected response -- $r"))
        }
      }
    } catch {
      case e: Exception => p.failure(e)
    }

    p.future
  }

  private def getRemoteContent(client: ChannelClient, connection: Connection, query: String): Future[Content] = {
    val p = Promise[Content]()

    try {
      client.query[Content](query, local = false, connectionIids = List(connection.iid)) {
        case QueryResponse(
          QueryResponseType.Query,
          _,
          tpe,
          _,
          JArray(i :: Nil),
          _,
          _,
          None
        ) if tpe =:= "Content" =>
          p.success(Content.fromJson(i))
        case r => p.failure(new Exception(s"Unexpected response -- $r"))
      }
    } catch {
      case e: Exception => p.failure(e)
    }

    p.future
  }
}
