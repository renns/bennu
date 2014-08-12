package com.qoid.bennu.model.assist

import com.google.inject.Inject
import com.google.inject.Singleton
import com.qoid.bennu.distributed.DistributedManager
import com.qoid.bennu.model.Alias
import com.qoid.bennu.model.Connection
import com.qoid.bennu.model.Label
import com.qoid.bennu.model.LabelAcl
import com.qoid.bennu.model.LabelChild
import com.qoid.bennu.model.id.InternalId
import com.qoid.bennu.model.id.PeerId
import com.qoid.bennu.security.Role
import com.qoid.bennu.security.SecurityContext
import m3.predef._

@Singleton
class ConnectionAssist @Inject()(
  injector: ScalaInjector,
  labelAssist: LabelAssist,
  distributedMgr: DistributedManager
) {

  def createConnection(
    aliasIid: InternalId,
    localPeerId: PeerId,
    remotePeerId: PeerId,
    connectionsLabelIid: InternalId,
    connectionIid: InternalId = InternalId.random
  ): Connection = {

    // Create connection label
    val connectionLabel = Label.insert(Label(labelAssist.connectionLabelName, data = labelAssist.metaLabelData))
    LabelChild.insert(LabelChild(connectionsLabelIid, connectionLabel.iid))

    // Create connection
    val connection = Connection.insert(Connection(aliasIid, localPeerId, remotePeerId, connectionLabel.iid, iid = connectionIid))

    // Grant the ContentViewer role to the connection meta-label
    LabelAcl.insert(LabelAcl(connection.iid, connectionLabel.iid, Role.ContentViewer, 1))

    // Grant the ProfileViewer role to the alias' label (to 2 degrees of visibility)
    val alias = Alias.fetch(aliasIid)
    LabelAcl.insert(LabelAcl(connection.iid, alias.labelIid, Role.ProfileViewer, 2))

    // Start listening on the connection
    distributedMgr.listen(connection)

    connection
  }

  def createConnection(
    localPeerId: PeerId,
    remotePeerId: PeerId
  ): Connection = {

    val securityContext = injector.instance[SecurityContext]

    val alias = Alias.fetch(securityContext.aliasIid)
    val label = Label.fetch(alias.labelIid)

    val metaLabel = labelAssist.findChildLabel(label.iid, labelAssist.metaLabelName).open_$
    val connectionsLabel = labelAssist.findChildLabel(metaLabel.iid, labelAssist.connectionsLabelName).open_$

    createConnection(securityContext.aliasIid, localPeerId, remotePeerId, connectionsLabel.iid, InternalId.random)
  }
}
