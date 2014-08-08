package com.qoid.bennu.integration

import com.qoid.bennu.client._
import org.specs2.Specification
import org.specs2.execute.Result

import scala.async.Async

class LabelSpec extends Specification {
  implicit val config = HttpClientConfig()

  def is = s2"""
    ${section("integration")}

    Label should
      create label          ${createLabel()}
      update label          ${updateLabel()}
      move label            ${moveLabel()}
      copy label            ${copyLabel()}
      remove label          ${removeLabel()}
      grant label access    ${grantLabelAccess()}
      revoke label access   ${revokeLabelAccess()}
      update label access   ${updateLabelAccess()}

    ${section("integration")}
  """

  //TODO: Test error codes coming back from service
  //TODO: Test sending to another agent
  //TODO: Test DoV

  def createLabel(): Result = {
    ClientAssist.channelClient1 { client =>
      Async.async {
        val rootLabel = Async.await(client.getCurrentAliasLabel())
        val name = "Label"
        val label = Async.await(client.createLabel(rootLabel.iid, name))

        label.name must_== name
      }
    }.await(60)
  }

  def updateLabel(): Result = {
    ClientAssist.channelClient1 { client =>
      Async.async {
        val rootLabel = Async.await(client.getCurrentAliasLabel())
        val label = Async.await(client.createLabel(rootLabel.iid, "Label"))
        val name = "Label2"
        val label2 = Async.await(client.updateLabel(label.iid, name))

        label2.name must_== name
      }
    }.await(60)
  }

  def moveLabel(): Result = {
    ClientAssist.channelClient1 { client =>
      Async.async {
        val rootLabel = Async.await(client.getCurrentAliasLabel())
        val label1 = Async.await(client.createLabel(rootLabel.iid, "Label1"))
        val label2 = Async.await(client.createLabel(rootLabel.iid, "Label2"))
        val label3 = Async.await(client.createLabel(label1.iid, "Label3"))
        val labelIid = Async.await(client.moveLabel(label3.iid, label1.iid, label2.iid))

        labelIid must_== label3.iid
      }
    }.await(60)
  }

  def copyLabel(): Result = {
    ClientAssist.channelClient1 { client =>
      Async.async {
        val rootLabel = Async.await(client.getCurrentAliasLabel())
        val label1 = Async.await(client.createLabel(rootLabel.iid, "Label1"))
        val label2 = Async.await(client.createLabel(rootLabel.iid, "Label2"))
        val label3 = Async.await(client.createLabel(label1.iid, "Label3"))
        val labelIid = Async.await(client.copyLabel(label3.iid, label2.iid))

        labelIid must_== label3.iid
      }
    }.await(60)
  }

  def removeLabel(): Result = {
    ClientAssist.channelClient1 { client =>
      Async.async {
        val rootLabel = Async.await(client.getCurrentAliasLabel())
        val label = Async.await(client.createLabel(rootLabel.iid, "Label"))
        val labelIid = Async.await(client.removeLabel(label.iid, rootLabel.iid))

        labelIid must_== label.iid
      }
    }.await(60)
  }

  def grantLabelAccess(): Result = {
    ClientAssist.channelClient1 { client =>
      Async.async {
        val rootLabel = Async.await(client.getCurrentAliasLabel())
        val label = Async.await(client.createLabel(rootLabel.iid, "Label"))

        val alias = Async.await(client.getCurrentAlias())
        val connections = Async.await(client.getConnections())
        val connection = connections.find(_.iid != alias.connectionIid).head

        val labelIid = Async.await(client.grantLabelAccess(label.iid, connection.iid, 1))

        labelIid must_== label.iid
      }
    }.await(60)
  }

  def revokeLabelAccess(): Result = {
    ClientAssist.channelClient1 { client =>
      Async.async {
        val rootLabel = Async.await(client.getCurrentAliasLabel())
        val label = Async.await(client.createLabel(rootLabel.iid, "Label"))

        val alias = Async.await(client.getCurrentAlias())
        val connections = Async.await(client.getConnections())
        val connection = connections.find(_.iid != alias.connectionIid).head

        Async.await(client.grantLabelAccess(label.iid, connection.iid, 1))
        val labelIid = Async.await(client.revokeLabelAccess(label.iid, connection.iid))

        labelIid must_== label.iid
      }
    }.await(60)
  }

  def updateLabelAccess(): Result = {
    ClientAssist.channelClient1 { client =>
      Async.async {
        val rootLabel = Async.await(client.getCurrentAliasLabel())
        val label = Async.await(client.createLabel(rootLabel.iid, "Label"))

        val alias = Async.await(client.getCurrentAlias())
        val connections = Async.await(client.getConnections())
        val connection = connections.find(_.iid != alias.connectionIid).head

        Async.await(client.grantLabelAccess(label.iid, connection.iid, 1))
        val labelIid = Async.await(client.updateLabelAccess(label.iid, connection.iid, 2))

        labelIid must_== label.iid
      }
    }.await(60)
  }
}
