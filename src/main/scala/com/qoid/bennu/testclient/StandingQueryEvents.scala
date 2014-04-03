package com.qoid.bennu.testclient

import com.qoid.bennu.JsonAssist._
import com.qoid.bennu.JsonAssist.jsondsl._
import com.qoid.bennu.model._
import com.qoid.bennu.model.id.PeerId
import com.qoid.bennu.model.introduction.IntroductionState
import com.qoid.bennu.model.notification.NotificationKind
import com.qoid.bennu.testclient.client.HttpAssist.HttpClientConfig
import com.qoid.bennu.testclient.client._
import m3.guice.GuiceApp
import m3.jdbc._
import net.model3.logging._
import scala.collection.mutable

object StandingQueryEvents extends GuiceApp {
  LoggerHelper.getRootLogger.setLevel(Level.INFO)
  implicit val config = HttpAssist.HttpClientConfig()

  try {
    println("\n" + "Create Alias:"); createAlias()
    println("\n" + "Update Alias:"); updateAlias()
    println("\n" + "Delete Alias:"); deleteAlias()
    println("\n" + "Create Connection:"); createConnection()
    println("\n" + "Delete Connection:"); deleteConnection()
    println("\n" + "Create Content:"); createContent()
    println("\n" + "Update Content:"); updateContent()
    println("\n" + "Create Label:"); createLabel()
    println("\n" + "Update Label:"); updateLabel()
    println("\n" + "Create LabelAcl:"); createLabelAcl()
    println("\n" + "Delete LabelAcl:"); deleteLabelAcl()
    println("\n" + "Create LabelChild:"); createLabelChild()
    println("\n" + "Delete LabelChild:"); deleteLabelChild()
    println("\n" + "Create LabeledContent:"); createLabeledContent()
    println("\n" + "Delete LabeledContent:"); deleteLabeledContent()
    println("\n" + "Update Notification:"); updateNotification()
    println("\n" + "Update Profile:"); updateProfile()
  } catch {
    case e: Exception => logger.warn(e)
  }

  System.exit(0)

  def createAlias()(implicit config: HttpClientConfig): Unit = {
    val responses = new mutable.ListBuffer[QueryResponse]

    val client = HttpAssist.createAgent("Agent1")
    val rootLabel = client.getRootLabel()

    setupStandingQueries(client, responses)

    client.createAlias(rootLabel.iid, "Alias")

    Thread.sleep(5000)
    printResponses(client, responses)
  }

  def updateAlias()(implicit config: HttpClientConfig): Unit = {
    val responses = new mutable.ListBuffer[QueryResponse]

    val client = HttpAssist.createAgent("Agent1")
    val rootLabel = client.getRootLabel()
    val alias = client.createAlias(rootLabel.iid, "Alias")

    setupStandingQueries(client, responses)

    client.updateAlias(alias.iid, "Alias2")

    Thread.sleep(5000)
    printResponses(client, responses)
  }

  def deleteAlias()(implicit config: HttpClientConfig): Unit = {
    val responses = new mutable.ListBuffer[QueryResponse]

    val client = HttpAssist.createAgent("Agent1")
    val rootLabel = client.getRootLabel()
    val alias = client.createAlias(rootLabel.iid, "Alias")
    client.createConnection(alias.iid, PeerId.random, PeerId.random)

    setupStandingQueries(client, responses)

    client.deleteAlias(alias.iid)

    Thread.sleep(5000)
    printResponses(client, responses)
  }

  def createConnection()(implicit config: HttpClientConfig): Unit = {
    val responses = new mutable.ListBuffer[QueryResponse]

    val client = HttpAssist.createAgent("Agent1")
    val rootAlias = client.getRootAlias()

    setupStandingQueries(client, responses)

    client.createConnection(rootAlias.iid, PeerId.random, PeerId.random)

    Thread.sleep(5000)
    printResponses(client, responses)
  }

  def deleteConnection()(implicit config: HttpClientConfig): Unit = {
    val responses = new mutable.ListBuffer[QueryResponse]

    val client = HttpAssist.createAgent("Agent1")
    val rootAlias = client.getRootAlias()
    val rootLabel = client.getRootLabel()
    val connection = client.createConnection(rootAlias.iid, PeerId.random, PeerId.random)
    client.upsert(Introduction(connection.iid, IntroductionState.NotResponded, connection.iid, IntroductionState.NotResponded))
    client.grantAccess(connection.iid, rootLabel.iid)
    client.upsert(Notification(connection.iid, NotificationKind.IntroductionRequest))

    setupStandingQueries(client, responses)

    client.delete[Connection](connection.iid)

    Thread.sleep(5000)
    printResponses(client, responses)
  }

  def createContent()(implicit config: HttpClientConfig): Unit = {
    val responses = new mutable.ListBuffer[QueryResponse]

    val client = HttpAssist.createAgent("Agent1")
    val rootAlias = client.getRootAlias()

    setupStandingQueries(client, responses)

    client.createContent(rootAlias.iid, "TEXT", "text" -> "Content", Some(List(rootAlias.rootLabelIid)))

    Thread.sleep(5000)
    printResponses(client, responses)
  }

  def updateContent()(implicit config: HttpClientConfig): Unit = {
    val responses = new mutable.ListBuffer[QueryResponse]

    val client = HttpAssist.createAgent("Agent1")
    val rootAlias = client.getRootAlias()
    val content = client.createContent(rootAlias.iid, "TEXT", "text" -> "Content", Some(List(rootAlias.rootLabelIid)))

    setupStandingQueries(client, responses)

    client.updateContent(content.iid, "text" -> "Content2")

    Thread.sleep(5000)
    printResponses(client, responses)
  }

  def createLabel()(implicit config: HttpClientConfig): Unit = {
    val responses = new mutable.ListBuffer[QueryResponse]

    val client = HttpAssist.createAgent("Agent1")
    val rootLabel = client.getRootLabel()

    setupStandingQueries(client, responses)

    client.createLabel(rootLabel.iid, "Label")

    Thread.sleep(5000)
    printResponses(client, responses)
  }

  def updateLabel()(implicit config: HttpClientConfig): Unit = {
    val responses = new mutable.ListBuffer[QueryResponse]

    val client = HttpAssist.createAgent("Agent1")
    val rootLabel = client.getRootLabel()
    val label = client.createLabel(rootLabel.iid, "Label")

    setupStandingQueries(client, responses)

    client.updateLabel(label.iid, "Label2", label.data)

    Thread.sleep(5000)
    printResponses(client, responses)
  }

  def createLabelAcl()(implicit config: HttpClientConfig): Unit = {
    val responses = new mutable.ListBuffer[QueryResponse]

    val client = HttpAssist.createAgent("Agent1")
    val rootLabel = client.getRootLabel()
    val rootAlias = client.getRootAlias()
    val connection = client.createConnection(rootAlias.iid, PeerId.random, PeerId.random)

    setupStandingQueries(client, responses)

    client.grantAccess(connection.iid, rootLabel.iid)

    Thread.sleep(5000)
    printResponses(client, responses)
  }

  def deleteLabelAcl()(implicit config: HttpClientConfig): Unit = {
    val responses = new mutable.ListBuffer[QueryResponse]

    val client = HttpAssist.createAgent("Agent1")
    val rootLabel = client.getRootLabel()
    val rootAlias = client.getRootAlias()
    val connection = client.createConnection(rootAlias.iid, PeerId.random, PeerId.random)
    client.grantAccess(connection.iid, rootLabel.iid)

    setupStandingQueries(client, responses)

    client.revokeAccess(connection.iid, rootLabel.iid)

    Thread.sleep(5000)
    printResponses(client, responses)
  }

  def createLabelChild()(implicit config: HttpClientConfig): Unit = {
    val responses = new mutable.ListBuffer[QueryResponse]

    val client = HttpAssist.createAgent("Agent1")
    val rootLabel = client.getRootLabel()
    val label1 = client.createLabel(rootLabel.iid, "Label1")
    val label2 = client.createLabel(rootLabel.iid, "Label2")

    setupStandingQueries(client, responses)

    client.copyLabel(label1.iid, label2.iid)

    Thread.sleep(5000)
    printResponses(client, responses)
  }

  def deleteLabelChild()(implicit config: HttpClientConfig): Unit = {
    val responses = new mutable.ListBuffer[QueryResponse]

    val client = HttpAssist.createAgent("Agent1")
    val rootLabel = client.getRootLabel()
    val label = client.createLabel(rootLabel.iid, "Label")

    setupStandingQueries(client, responses)

    client.deleteLabel(rootLabel.iid, label.iid)

    Thread.sleep(5000)
    printResponses(client, responses)
  }

  def createLabeledContent()(implicit config: HttpClientConfig): Unit = {
    val responses = new mutable.ListBuffer[QueryResponse]

    val client = HttpAssist.createAgent("Agent1")
    val rootAlias = client.getRootAlias()
    val content = client.createContent(rootAlias.iid, "TEXT", "text" -> "Content", Some(List(rootAlias.rootLabelIid)))
    val label = client.createLabel(rootAlias.rootLabelIid, "Label")

    setupStandingQueries(client, responses)

    client.addLabelToContent(content.iid, label.iid)

    Thread.sleep(5000)
    printResponses(client, responses)
  }

  def deleteLabeledContent()(implicit config: HttpClientConfig): Unit = {
    val responses = new mutable.ListBuffer[QueryResponse]

    val client = HttpAssist.createAgent("Agent1")
    val rootAlias = client.getRootAlias()
    val content = client.createContent(rootAlias.iid, "TEXT", "text" -> "Content", Some(List(rootAlias.rootLabelIid)))
    val label = client.createLabel(rootAlias.rootLabelIid, "Label")
    client.addLabelToContent(content.iid, label.iid)

    setupStandingQueries(client, responses)

    client.removeLabelFromContent(content.iid, label.iid)

    Thread.sleep(5000)
    printResponses(client, responses)
  }

  def updateNotification()(implicit config: HttpClientConfig): Unit = {
    val responses = new mutable.ListBuffer[QueryResponse]

    val client = HttpAssist.createAgent("Agent1")
    val rootAlias = client.getRootAlias()
    val connection = client.createConnection(rootAlias.iid, PeerId.random, PeerId.random)
    val notification = client.upsert(Notification(connection.iid, NotificationKind.IntroductionRequest))

    setupStandingQueries(client, responses)

    client.consumeNotification(notification.iid)

    Thread.sleep(5000)
    printResponses(client, responses)
  }

  def updateProfile()(implicit config: HttpClientConfig): Unit = {
    val responses = new mutable.ListBuffer[QueryResponse]

    val client = HttpAssist.createAgent("Agent1")
    val rootAlias = client.getRootAlias()
    val alias = client.createAlias(rootAlias.rootLabelIid, "Alias")

    setupStandingQueries(client, responses)

    client.updateProfile(alias.iid, "Alias2", "")

    Thread.sleep(5000)
    printResponses(client, responses)
  }

  private def setupStandingQueries(client: ChannelClient, responses: mutable.ListBuffer[QueryResponse]): Unit = {
    client.query[Alias]("", historical = false, standing = true)(handleQueryResponse(_, responses))
    client.query[Connection]("", historical = false, standing = true)(handleQueryResponse(_, responses))
    client.query[Content]("", historical = false, standing = true)(handleQueryResponse(_, responses))
    client.query[Introduction]("", historical = false, standing = true)(handleQueryResponse(_, responses))
    client.query[Label]("", historical = false, standing = true)(handleQueryResponse(_, responses))
    client.query[LabelAcl]("", historical = false, standing = true)(handleQueryResponse(_, responses))
    client.query[LabelChild]("", historical = false, standing = true)(handleQueryResponse(_, responses))
    client.query[LabeledContent]("", historical = false, standing = true)(handleQueryResponse(_, responses))
    client.query[Notification]("", historical = false, standing = true)(handleQueryResponse(_, responses))
    client.query[Profile]("", historical = false, standing = true)(handleQueryResponse(_, responses))
  }

  private def printResponses(client: ChannelClient, responses: mutable.ListBuffer[QueryResponse]): Unit = {
    for (response <- responses) {
      response match {
        case QueryResponse(_, _, "alias", _, JArray(r :: Nil), _, _, Some(action)) =>
          println(s"$action Alias")
        case QueryResponse(_, _, "connection", _, JArray(r :: Nil), _, _, Some(action)) =>
          println(s"$action Connection")
        case QueryResponse(_, _, "content", _, JArray(r :: Nil), _, _, Some(action)) =>
          println(s"$action Content")
        case QueryResponse(_, _, "label", _, JArray(r :: Nil), _, _, Some(action)) =>
          val i = Label.fromJson(r)
          println(s"$action Label (${i.name})")
        case QueryResponse(_, _, "labelacl", _, JArray(r :: Nil), _, _, Some(action)) =>
          println(s"$action LabelAcl")
        case QueryResponse(_, _, "labelchild", _, JArray(r :: Nil), _, _, Some(action)) =>
          val i = LabelChild.fromJson(r)
          val parent = client.queryLocal[Label](sql"iid = ${i.parentIid}")
          val child = client.queryLocal[Label](sql"iid = ${i.childIid}")

          (parent, child) match {
            case (p :: Nil, c :: Nil) => println(s"$action LabelChild (${p.name} > ${c.name})")
            case _ => println(s"$action LabelChild (${i.parentIid.value} > ${i.childIid.value})")
          }
        case QueryResponse(_, _, "labeledcontent", _, JArray(r :: Nil), _, _, Some(action)) =>
          println(s"$action LabeledContent")
        case QueryResponse(_, _, "introduction", _, JArray(r :: Nil), _, _, Some(action)) =>
          println(s"$action Introduction")
        case QueryResponse(_, _, "notification", _, JArray(r :: Nil), _, _, Some(action)) =>
          val i = Notification.fromJson(r)
          println(s"$action Notification (${i.kind})")
        case QueryResponse(_, _, "profile", _, JArray(r :: Nil), _, _, Some(action)) =>
          println(s"$action Profile")
        case r => println(r)
      }
    }
  }

  private def handleQueryResponse(response: QueryResponse, responses: mutable.ListBuffer[QueryResponse]): Unit = {
    responses.append(response)
  }
}
