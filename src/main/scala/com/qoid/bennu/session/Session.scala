package com.qoid.bennu.session

import com.qoid.bennu.model.id.InternalId
import com.qoid.bennu.security.AliasSecurityContext
import com.qoid.bennu.security.SecurityContext
import m3.LockFreeMap
import m3.predef._
import m3.predef.box._
import m3.servlet.longpoll.ChannelId
import m3.servlet.longpoll.JettyChannelManager.JettyContinuationChannel

class Session(injector: ScalaInjector, val aliasIid: InternalId) extends Logging {
  private val data = LockFreeMap[String, Any]()

  val channel: JettyContinuationChannel = createChannel()
  val securityContext: SecurityContext = AliasSecurityContext(injector, aliasIid)

  //TODO: store reference to standing queries

  def put[T : Manifest](key: String, value: T): Box[T] = {
    data.put(key, value) match {
      case Some(v: T) => Full(v)
      case Some(v) => Failure(s"session data not specified type -- expected: ${manifest[T].runtimeClass.getName} -- actual: ${v.getClass.getName}")
      case _ => Empty
    }
  }

  def get[T : Manifest](key: String): Box[T] = {
    data.get(key) match {
      case Some(v: T) => Full(v)
      case Some(v) => Failure(s"session data not specified type -- expected: ${manifest[T].runtimeClass.getName} -- actual: ${v.getClass.getName}")
      case _ => Empty
    }
  }

  def remove[T : Manifest](key: String): Box[T] = {
    data.remove(key) match {
      case Some(v: T) => Full(v)
      case Some(v) => Failure(s"session data not specified type -- expected: ${manifest[T].runtimeClass.getName} -- actual: ${v.getClass.getName}")
      case _ => Empty
    }
  }

  def close(): Unit = {
    //cancel any queries, etc
  }

  private def createChannel(): JettyContinuationChannel = {
    val channelId = ChannelId.random()
    logger.debug(s"creating channel ${channelId.value}")
    JettyContinuationChannel(channelId)
  }
}
