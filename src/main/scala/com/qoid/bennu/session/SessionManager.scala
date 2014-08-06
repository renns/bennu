package com.qoid.bennu.session

import java.util.concurrent.TimeUnit

import com.google.inject.Inject
import com.google.inject.Singleton
import com.qoid.bennu.BennuException
import com.qoid.bennu.Config
import com.qoid.bennu.ErrorCode
import com.qoid.bennu.model.Alias
import com.qoid.bennu.model.id.AuthenticationId
import com.qoid.bennu.model.id.InternalId
import com.qoid.bennu.security.AuthenticationManager
import com.qoid.bennu.security.ConnectionSecurityContext
import com.qoid.bennu.security.SecurityContext
import io.netty.util.HashedWheelTimer
import io.netty.util.Timeout
import io.netty.util.TimerTask
import m3.LockFreeMap
import m3.predef._
import m3.servlet.HttpResponseException
import m3.servlet.HttpStatusCodes
import m3.servlet.longpoll.ChannelId

@Singleton
class SessionManager @Inject()(
  injector: ScalaInjector,
  authMgr: AuthenticationManager,
  config: Config
) {

  private val sessions = LockFreeMap[ChannelId, Session]()
  private val timer = new HashedWheelTimer(10, TimeUnit.SECONDS)
  private val timeouts = LockFreeMap.empty[ChannelId, Timeout]

  def createSession(authenticationId: AuthenticationId, password: String): Session = {
    val connectionIid = authMgr.authenticate(authenticationId, password)
    val securityContext = new ConnectionSecurityContext(connectionIid, injector)
    val session = new Session(injector, securityContext)
    sessions.put(session.channel.id, session)
    timeouts.put(session.channel.id, createTimeout(session.channel.id))
    session
  }

  def createSession(aliasIid: InternalId): Session = {
    val currentSecurityContext = injector.instance[SecurityContext]

    if (currentSecurityContext.canSpawnSession) {
      val alias = Alias.fetch(aliasIid)
      val securityContext = new ConnectionSecurityContext(alias.connectionIid, injector)
      val session = new Session(injector, securityContext)
      sessions.put(session.channel.id, session)
      timeouts.put(session.channel.id, createTimeout(session.channel.id))
      session
    } else {
      throw new BennuException(ErrorCode.permissionDenied, "canSpawnSession")
    }
  }

  def getSessionOpt(channelId: ChannelId): Option[Session] = {
    val sessionOpt = sessions.get(channelId)
    sessionOpt.foreach(session => timeouts.put(session.channel.id, createTimeout(session.channel.id)).foreach(_.cancel()))
    sessionOpt
  }

  def getSession(channelId: ChannelId): Session = {
    getSessionOpt(channelId).getOrElse(throw new HttpResponseException(HttpStatusCodes.UNAUTHORIZED))
  }

  def closeSession(channelId: ChannelId): Unit = {
    sessions.remove(channelId).foreach(_.close())
    timeouts.remove(channelId)
  }

  private def createTimeout(channelId: ChannelId): Timeout = {
    timer.newTimeout(getTimerTask(channelId), config.sessionTimeoutSeconds, TimeUnit.MINUTES)
  }

  private def getTimerTask(channelId: ChannelId): TimerTask = {
    new TimerTask {
      override def run(t: Timeout): Unit = {
        closeSession(channelId)
      }
    }
  }
}
