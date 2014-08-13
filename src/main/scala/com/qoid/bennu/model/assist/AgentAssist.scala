package com.qoid.bennu.model.assist

import java.sql.{Connection => JdbcConn}

import com.google.inject.Inject
import com.google.inject.Singleton
import com.qoid.bennu.BennuException
import com.qoid.bennu.ErrorCode
import com.qoid.bennu.JsonAssist._
import com.qoid.bennu.JsonAssist.jsondsl._
import com.qoid.bennu.distributed.DistributedManager
import com.qoid.bennu.mapper.MapperAssist
import com.qoid.bennu.model._
import com.qoid.bennu.model.id.InternalId
import com.qoid.bennu.model.id.PeerId
import com.qoid.bennu.security.AgentSecurityContext
import com.qoid.bennu.security.ConnectionSecurityContext
import com.qoid.bennu.security.AuthenticationManager
import com.qoid.bennu.security.Role
import com.qoid.bennu.security.SecurityContext
import com.qoid.bennu.security.SystemSecurityContext
import m3.jdbc._
import m3.predef._

@Singleton
class AgentAssist @Inject()(
  injector: ScalaInjector,
  authenticationMgr: AuthenticationManager,
  distributedMgr: DistributedManager,
  aliasAssist: AliasAssist,
  connectionAssist: ConnectionAssist
) {

  private val introducerAgentName = "Introducer"
  private val introducerPassword = "introducer"

  def validateAgentName(name: String): Unit = {
    if (name.isEmpty || name.contains(".")) {
      throw new BennuException(ErrorCode.nameInvalid)
    }

    SystemSecurityContext {
      if (Agent.select(sql"lower(name) = ${name.toLowerCase}").nonEmpty) {
        throw new BennuException(ErrorCode.nameDuplicate)
      }
    }
  }

  def createAgent(name: String, password: String): Login = {
    validateAgentName(name)
    authenticationMgr.validatePassword(password)

    val securityContext = injector.instance[SecurityContext]

    val agent = Agent.insert(Agent(name, aliasIid = InternalId.random))
    val alias = aliasAssist.createRootAlias(name, name, "", securityContext.connectionIid, agent.aliasIid)
    val login = authenticationMgr.createLogin(alias.iid, password)

    login
  }

  def deleteAgent(): Unit = {
    val securityContext = injector.instance[SecurityContext]

    val agent = Agent.selectOne(sql"agentId = ${securityContext.agentId}")

    if (securityContext.canDelete[Agent](agent)) {
      val jdbcConn = injector.instance[JdbcConn]

      // The following is executed outside of any security context
      MapperAssist.allMappers.foreach { mapper =>
        jdbcConn.update(sql"delete from ${mapper.tableName.rawSql} where agentId = ${securityContext.agentId}")
        jdbcConn.update(sql"delete from ${(mapper.tableName + "_log").rawSql} where agentId = ${securityContext.agentId}")
      }
    } else {
      throw new BennuException(ErrorCode.permissionDenied, "delete Agent")
    }
  }

  def importAgent(agentData: JValue): Agent = {
    val agent = Agent.fromJson(agentData \ "agent")

    val aliases = agentData \ "aliases" match {
      case JArray(x) => x.map(Alias.fromJson)
      case _ => throw new BennuException(ErrorCode.importAgentFailed, "parsing aliases")
    }

    val connections = agentData \ "connections" match {
      case JArray(x) => x.map(Connection.fromJson)
      case _ => throw new BennuException(ErrorCode.importAgentFailed, "parsing connections")
    }

    val contents = agentData \ "contents" match {
      case JArray(x) => x.map(Content.fromJson)
      case _ => throw new BennuException(ErrorCode.importAgentFailed, "parsing contents")
    }

    val introductions = agentData \ "introductions" match {
      case JArray(x) => x.map(Introduction.fromJson)
      case _ => throw new BennuException(ErrorCode.importAgentFailed, "parsing introductions")
    }

    val labels = agentData \ "labels" match {
      case JArray(x) => x.map(Label.fromJson)
      case _ => throw new BennuException(ErrorCode.importAgentFailed, "parsing labels")
    }

    val labelAcls = agentData \ "labelAcls" match {
      case JArray(x) => x.map(LabelAcl.fromJson)
      case _ => throw new BennuException(ErrorCode.importAgentFailed, "parsing labelAcls")
    }

    val labelChilds = agentData \ "labelChilds" match {
      case JArray(x) => x.map(LabelChild.fromJson)
      case _ => throw new BennuException(ErrorCode.importAgentFailed, "parsing labelChilds")
    }

    val labeledContents = agentData \ "labeledContents" match {
      case JArray(x) => x.map(LabeledContent.fromJson)
      case _ => throw new BennuException(ErrorCode.importAgentFailed, "parsing labeledContents")
    }

    val logins = agentData \ "logins" match {
      case JArray(x) => x.map(Login.fromJson)
      case _ => throw new BennuException(ErrorCode.importAgentFailed, "parsing logins")
    }

    val notifications = agentData \ "notifications" match {
      case JArray(x) => x.map(Notification.fromJson)
      case _ => throw new BennuException(ErrorCode.importAgentFailed, "parsing notifications")
    }

    val profiles = agentData \ "profiles" match {
      case JArray(x) => x.map(Profile.fromJson)
      case _ => throw new BennuException(ErrorCode.importAgentFailed, "parsing profiles")
    }

    AgentSecurityContext(agent.agentId, agent.createdByConnectionIid) {
      Agent.rawInsert(agent)
      aliases.foreach(Alias.rawInsert)
      connections.foreach(Connection.rawInsert)
      contents.foreach(Content.rawInsert)
      introductions.foreach(Introduction.rawInsert)
      labels.foreach(Label.rawInsert)
      labelAcls.foreach(LabelAcl.rawInsert)
      labelChilds.foreach(LabelChild.rawInsert)
      labeledContents.foreach(LabeledContent.rawInsert)
      logins.foreach(Login.rawInsert)
      notifications.foreach(Notification.rawInsert)
      profiles.foreach(Profile.rawInsert)

      distributedMgr.listen(connections)
    }

    agent
  }

  def exportAgent(): JValue = {
    val securityContext = injector.instance[SecurityContext]

    if (securityContext.canExportAgent) {
      val agent = Agent.selectOne("")
      val aliases = Alias.select("").toList
      val connections = Connection.select("").toList
      val contents = Content.select("").toList
      val introductions = Introduction.select("").toList
      val labels = Label.select("").toList
      val labelAcls = LabelAcl.select("").toList
      val labelChilds = LabelChild.select("").toList
      val labeledContents = LabeledContent.select("").toList
      val logins = Login.select("").toList
      val notifications = Notification.select("").toList
      val profiles = Profile.select("").toList

      ("agent" -> agent.toJson) ~
      ("aliases" -> aliases.map(_.toJson)) ~
      ("connections" -> connections.map(_.toJson)) ~
      ("contents" -> contents.map(_.toJson)) ~
      ("introductions" -> introductions.map(_.toJson)) ~
      ("labels" -> labels.map(_.toJson)) ~
      ("labelAcls" -> labelAcls.map(_.toJson)) ~
      ("labelChilds" -> labelChilds.map(_.toJson)) ~
      ("labeledContents" -> labeledContents.map(_.toJson)) ~
      ("logins" -> logins.map(_.toJson)) ~
      ("notifications" -> notifications.map(_.toJson)) ~
      ("profiles" -> profiles.map(_.toJson))
    } else {
      throw new BennuException(ErrorCode.permissionDenied, "export Agent")
    }
  }

  def createIntroducerAgent(): Login = createAgent(introducerAgentName, introducerPassword)

  def connectToIntroducer(connectionIid: InternalId): Unit = {
    val introducerConnectionIid = SystemSecurityContext {
      val agent = Agent.selectOne(sql"name = ${introducerAgentName}")
      val alias = Alias.fetch(agent.aliasIid)
      alias.connectionIid
    }

    val peerId1 = PeerId.random
    val peerId2 = PeerId.random

    ConnectionSecurityContext(connectionIid, 1, injector) {
      connectionAssist.createConnection(peerId1, peerId2)
    }

    ConnectionSecurityContext(introducerConnectionIid, 1, injector) {
      connectionAssist.createConnection(peerId2, peerId1)
    }
  }
}
